"""Checks for installation integrity, setting rollback, and shell quoting."""

import hashlib
import importlib.util
import io
import json
import runpy
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location(
    "contour_flow_install", Path(__file__).with_name("install.py")
)
installer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(installer)


class InstallTests(unittest.TestCase):
    def setUp(self):
        self.real_current_user = installer.current_user
        user = patch.object(installer, "current_user", return_value="0")
        self.user = user.start()
        self.addCleanup(user.stop)
        guard = patch(
            "subprocess.check_output",
            side_effect=AssertionError("Unexpected external command in unit test"),
        )
        guard.start()
        self.addCleanup(guard.stop)
        resolver = patch.object(installer.shutil, "which", return_value="/tools/adb")
        resolver.start()
        self.addCleanup(resolver.stop)

    def test_current_user_validation_and_failed_query(self):
        for value in ("0", "10", "00012"):
            with patch.object(installer, "adb", return_value=value) as command:
                self.assertEqual(str(int(value)), self.real_current_user("tv"))
            command.assert_called_once_with("tv", "shell", "am", "get-current-user")
        for value in (
            "",
            "-1",
            "0\n10",
            "Current user: 10",
            "2147483648",
            "99999999999",
        ):
            with (
                patch.object(installer, "adb", return_value=value),
                self.assertRaisesRegex(SystemExit, "ambiguous"),
            ):
                self.real_current_user("tv")
        with (
            patch.object(
                installer,
                "adb",
                side_effect=installer.subprocess.CalledProcessError(1, ["adb"]),
            ),
            self.assertRaisesRegex(SystemExit, "verify"),
        ):
            self.real_current_user("tv")

    def test_backup_and_restore_keep_the_same_nonzero_user(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.json"
            with patch.object(installer, "adb", return_value="null") as command:
                installer.save_original("tv", path, user="10")
            self.assertEqual(json.loads(path.read_text())["user"], "10")
            self.assertTrue(
                all(
                    call.args[3:5] == ("--user", "10")
                    for call in command.call_args_list
                )
            )
            with patch.object(installer, "set_setting") as setting:
                installer.restore("tv", path, user="10")
            self.assertTrue(
                all(call.kwargs == {"user": "10"} for call in setting.call_args_list)
            )
            for action in (installer.save_original, installer.restore):
                with (
                    patch.object(installer, "adb") as command,
                    self.assertRaisesRegex(SystemExit, "different Android user"),
                ):
                    action("tv", path, user="0")
                command.assert_not_called()

    def test_cli_captures_nonzero_user_before_confirmation(self):
        self.user.return_value = "10"
        with (
            patch.object(sys, "argv", ["install.py", "--device", "tv"]),
            patch.object(installer, "adb"),
            patch.object(installer, "install") as install,
            patch("builtins.input", return_value="no") as prompt,
        ):
            installer.main()
        self.user.assert_called_once_with("tv")
        self.assertIn("tv, Android user 10", prompt.call_args.args[0])
        install.assert_not_called()
        with (
            patch.object(sys, "argv", ["install.py", "--device", "tv", "--yes"]),
            patch.object(installer, "adb"),
            patch.object(installer, "install") as install,
        ):
            installer.main()
        install.assert_called_once_with(
            "tv", installer.HERE / "device-state.json", user="10"
        )

    def test_adb_rejects_invalid_devices_and_missing_executable(self):
        with patch("subprocess.check_output") as run:
            for serial in ("", "tv;injected", "-H", "tv with spaces"):
                with self.assertRaises(installer.argparse.ArgumentTypeError):
                    installer.adb(serial, "get-state")
            with (
                patch.object(installer.shutil, "which", return_value=None),
                self.assertRaisesRegex(SystemExit, "platform-tools"),
            ):
                installer.adb("tv", "get-state")
            run.assert_not_called()

    def test_missing_build_never_contacts_tv(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for apk_exists in (False, True):
                if apk_exists:
                    (root / "build").mkdir()
                    (root / "build/contour-flow.apk").write_bytes(b"build")
                with (
                    patch.object(installer, "HERE", root),
                    patch.object(installer, "adb") as adb,
                ):
                    with self.assertRaisesRegex(SystemExit, "Build first"):
                        installer.install("tv", root / "state.json")
                    adb.assert_not_called()

    def test_missing_restore_and_wrong_backup_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.json"
            with self.assertRaisesRegex(SystemExit, "No saved"):
                installer.restore("tv", path)
            path.write_text(json.dumps({"device": "other"}))
            with patch.object(installer, "adb") as adb:
                with self.assertRaisesRegex(SystemExit, "different device"):
                    installer.save_original("tv", path)
                adb.assert_not_called()

    def test_verified_activation_keeps_original_timeouts(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "build").mkdir()
            data = b"verified local build"
            (root / "build/contour-flow.apk").write_bytes(data)
            (root / "build/contour-flow.apk.sha256").write_text(
                hashlib.sha256(data).hexdigest()
            )
            with (
                patch.object(installer, "HERE", root),
                patch.object(installer, "save_original") as save,
                patch.object(installer, "adb", return_value="Success") as adb,
                patch.object(installer, "set_setting") as setting,
            ):
                installer.install("tv", root / "state.json")
                save.assert_called_once_with("tv", root / "state.json", user="0")
                self.assertEqual(
                    adb.call_args.args[1:6],
                    ("install", "--user", "0", "--no-incremental", "-r"),
                )
                self.assertEqual(
                    [call.args[1] for call in setting.call_args_list],
                    list(installer.KEYS),
                )

    def test_setting_write_is_read_back_and_failure_is_visible(self):
        with patch.object(installer, "adb", side_effect=["", "1"]) as adb:
            installer.set_setting("tv", "screensaver_enabled", "1")
            self.assertEqual(
                adb.call_args_list[0].args,
                (
                    "tv",
                    "shell",
                    "settings",
                    "--user",
                    "0",
                    "put",
                    "secure",
                    "screensaver_enabled",
                    "1",
                ),
            )
        with (
            patch.object(installer, "adb", side_effect=["", "0"]),
            self.assertRaisesRegex(SystemExit, "Device did not save"),
        ):
            installer.set_setting("tv", "screensaver_enabled", "1")

    def test_cli_routes_backups_and_rejects_unsafe_serials(self):
        for serial in ("", "tv;injected"):
            with (
                patch.object(sys, "argv", ["install.py", "--device", serial]),
                patch.object(installer, "adb") as adb,
                patch.object(sys, "stderr", io.StringIO()),
            ):
                with self.assertRaises(SystemExit):
                    installer.main()
                adb.assert_not_called()
        for restoring in (False, True):
            for custom in (False, True):
                args = ["install.py", "--device", "tv-1:5555", "--yes"]
                if restoring:
                    args.append("--restore")
                if custom:
                    args.extend(["--state-file", "second-tv.json"])
                with (
                    patch.object(sys, "argv", args),
                    patch.object(installer, "adb") as adb,
                    patch.object(installer, "install") as install,
                    patch.object(installer, "restore") as restore,
                ):
                    installer.main()
                    adb.assert_called_once_with("tv-1:5555", "get-state")
                    (restore if restoring else install).assert_called_once_with(
                        "tv-1:5555",
                        Path("second-tv.json")
                        if custom
                        else installer.HERE / "device-state.json",
                        user="0",
                    )
        with (
            patch.object(
                sys,
                "argv",
                [
                    "install.py",
                    "--device",
                    "tv-1:5555",
                    "--yes",
                    "--apk",
                    "release.apk",
                ],
            ),
            patch.object(installer, "adb"),
            patch.object(installer, "install") as install,
        ):
            installer.main()
            install.assert_called_once_with(
                "tv-1:5555",
                installer.HERE / "device-state.json",
                Path("release.apk"),
                user="0",
            )

    def test_confirmation_cancel_and_uninstall_restore_before_removal(self):
        self.assertTrue(installer.confirm("Install", True))
        with patch("builtins.input", return_value="yes"):
            self.assertTrue(installer.confirm("Install", False))
        with patch("builtins.input", return_value="no"):
            self.assertFalse(installer.confirm("Install", False))
        with (
            patch.object(sys, "argv", ["install.py", "--device", "tv-1:5555"]),
            patch.object(installer, "adb") as adb,
            patch.object(installer, "install") as install,
            patch("builtins.input", return_value="no"),
            patch("sys.stdout", io.StringIO()),
        ):
            installer.main()
            install.assert_not_called()
            adb.assert_called_once_with("tv-1:5555", "get-state")
        with (
            patch.object(
                sys,
                "argv",
                ["install.py", "--device", "tv-1:5555", "--uninstall", "--yes"],
            ),
            patch.object(installer, "adb", return_value="Success") as adb,
            patch.object(installer, "restore") as restore,
        ):
            installer.main()
            restore.assert_called_once_with(
                "tv-1:5555", installer.HERE / "device-state.json", user="0"
            )
            self.assertEqual(
                adb.call_args.args[1:], ("uninstall", "--user", "0", installer.PACKAGE)
            )

    def test_script_entry_point_validates_arguments_before_adb(self):
        with (
            patch.object(sys, "argv", ["install.py", "--device", "unsafe;serial"]),
            patch.object(sys, "stderr", io.StringIO()),
            patch("subprocess.check_output") as adb,
        ):
            with self.assertRaises(SystemExit):
                runpy.run_path(
                    str(Path(__file__).with_name("install.py")), run_name="__main__"
                )
            adb.assert_not_called()

    def test_checksum_mismatch_does_not_contact_device(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "build").mkdir()
            (root / "build/contour-flow.apk").write_bytes(b"changed APK bytes")
            (root / "build/contour-flow.apk.sha256").write_text(
                "0" * 64 + "  contour-flow.apk\n"
            )
            with (
                patch.object(installer, "HERE", root),
                patch.object(installer, "adb") as adb,
            ):
                with self.assertRaisesRegex(SystemExit, "checksum mismatch"):
                    installer.install("tv", root / "state.json")
                adb.assert_not_called()
            self.assertFalse((root / "state.json").exists())

    def test_shell_values_are_quoted(self):
        with patch("subprocess.check_output", return_value="1\n") as run:
            installer.adb(
                "tv", "shell", "settings", "put", "secure", "key", "x; echo injected"
            )
            self.assertEqual(
                run.call_args.args[0],
                [
                    "/tools/adb",
                    "-s",
                    "tv",
                    "shell",
                    "settings put secure key 'x; echo injected'",
                ],
            )

    def test_adb_state_arguments_are_not_shell_commands(self):
        with patch("subprocess.check_output", return_value="device\n") as run:
            self.assertEqual(installer.adb("tv", "get-state"), "device")
            self.assertEqual(
                run.call_args.args[0], ["/tools/adb", "-s", "tv", "get-state"]
            )
            installer.adb("tv")
            self.assertEqual(run.call_args.args[0], ["/tools/adb", "-s", "tv"])

    def test_backup_preserves_first_observed_settings(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.json"
            original = ("previous/.Dream", "0", "null")
            with patch.object(installer, "adb", side_effect=original):
                installer.save_original("tv", path)
            saved_bytes = path.read_bytes()
            with patch.object(installer, "adb") as adb:
                installer.save_original("tv", path)
                adb.assert_not_called()
            self.assertEqual(path.read_bytes(), saved_bytes)
            self.assertEqual(path.stat().st_mode & 0o777, 0o600)

    def test_multiple_tv_backups_remain_independent(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory) / "device-states"
            first, second = root / "first.json", root / "second.json"
            with patch.object(installer, "adb", side_effect=["first/.Dream", "1", "0"]):
                installer.save_original("first", first)
            first_bytes = first.read_bytes()
            with patch.object(
                installer, "adb", side_effect=["second/.Dream", "0", "null"]
            ):
                installer.save_original("second", second)
            self.assertEqual(first.read_bytes(), first_bytes)
            self.assertEqual(json.loads(second.read_text())["device"], "second")
            with patch.object(installer, "set_setting") as setting:
                installer.restore("second", second)
                self.assertEqual(
                    setting.call_args_list[0].args,
                    ("second", "screensaver_components", "second/.Dream"),
                )
            self.assertEqual(first.read_bytes(), first_bytes)

    def test_restore_deletes_originally_unset_setting(self):
        with patch.object(installer, "adb", side_effect=["", "null"]) as adb:
            installer.set_setting("tv", "screensaver_components", "null")
            self.assertEqual(
                adb.call_args_list[0].args,
                (
                    "tv",
                    "shell",
                    "settings",
                    "--user",
                    "0",
                    "delete",
                    "secure",
                    "screensaver_components",
                ),
            )

    def test_wrong_device_restore_does_not_change_settings(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "state.json"
            path.write_text(json.dumps({"device": "other-tv", "settings": {}}))
            with patch.object(installer, "adb") as adb:
                with self.assertRaisesRegex(SystemExit, "different device"):
                    installer.restore("tv", path)
                adb.assert_not_called()

    def test_failed_activation_restores_original_settings(self):
        import hashlib

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "build").mkdir()
            data = b"local build"
            (root / "build/contour-flow.apk").write_bytes(data)
            (root / "build/contour-flow.apk.sha256").write_text(
                hashlib.sha256(data).hexdigest()
            )
            failure = RuntimeError("device disconnected")
            with (
                patch.object(installer, "HERE", root),
                patch.object(installer, "save_original"),
                patch.object(installer, "adb", return_value="Success"),
                patch.object(installer, "set_setting", side_effect=[None, failure]),
                patch.object(installer, "restore") as restore,
            ):
                with self.assertRaisesRegex(RuntimeError, "disconnected"):
                    installer.install("tv", root / "state.json")
                restore.assert_called_once_with("tv", root / "state.json", user="0")


if __name__ == "__main__":
    unittest.main()
