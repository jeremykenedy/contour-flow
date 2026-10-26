"""Double precision reference for the unchanged frozen landscape, not a GPU emulator."""
import math
import re
from pathlib import Path
from types import SimpleNamespace

ROOT = Path(__file__).resolve().parents[1]
W, H = 1920, 1080
CORRECTION = re.search(r"float value=(.+);", (ROOT / "res/raw/contours_mesh.glsl").read_text()).group(1)

def field(u, v, t, relief):
    px = (u-.5)*4.1+.32*math.sin(t)
    py = (v-.5)*(4.1*H/W)+.24*math.cos(t)
    qx = px+.23*math.sin(py*1.6+t)
    qy = py+.18*math.sin(px*1.3-t)
    def hill(x,y,f):
        h = math.exp(-(x*x+y*y)*f)
        return h,-2*f*x*h,-2*f*y*h
    hills = [hill(qx+1.05+.16*math.sin(t),qy+.18,2.1),
             hill(qx-.22,qy-.34-.18*math.cos(t),2.8),
             hill(qx-1.25,qy+.62,2.3),hill(qx+.88,qy-.78,2.5),hill(qx-.23,qy+.24,2.0)]
    if relief == 0:
        h,gx,gy=.39,0.,0.; factors=(.27,.24,.24,.20,-.10)
    elif relief == 1:
        h,gx,gy=.62,0.,0.; factors=(-.25,-.22,.15,0.,-.19)
    else:
        h,gx,gy=.51-qy*.15,0.,-.15; factors=(.22,-.13,0.,.18,0.)
    for weight, value in zip(factors,hills):
        h+=weight*value[0];gx+=weight*value[1];gy+=weight*value[2]
    a=qx*2.4+.35*math.cos(qy*1.9)+t;b=qy*2.6-t
    h+=math.sin(a)*math.cos(b)*.065
    gx+=2.4*math.cos(a)*math.cos(b)*.065
    gy+=(-.665*math.sin(qy*1.9)*math.cos(a)*math.cos(b)-2.6*math.sin(a)*math.sin(b))*.065
    return h,(gx+gy*.234*math.cos(px*1.3-t))*4.1,(gy+gx*.368*math.cos(py*1.6+t))*(4.1*H/W)

def interpolate(u,v,t,relief,nx,ny,corrected=False):
    x=u*nx;y=v*ny;i=min(nx-1,int(x));j=min(ny-1,int(y));fx=x-i;fy=y-j
    if fx+fy<=1:
        weights=(1-fx-fy,fx,fy);points=((i,j),(i+1,j),(i,j+1))
    else:
        weights=(fx+fy-1,1-fx,1-fy);points=((i+1,j+1),(i,j+1),(i+1,j))
    f=[field(a/nx,b/ny,t,relief)for a,b in points]
    value=tuple(sum(w*p[k]for w,p in zip(weights,f))for k in range(3))
    if not corrected:
        return value
    dot=sum(w*(p[1]*a/nx+p[2]*b/ny)for w,p,(a,b)in zip(weights,f,points))
    data = SimpleNamespace(x=value[0], yz=value[1:], w=dot)
    corrected_value = eval(CORRECTION, {"__builtins__": {}}, {"vField": data, "uv": (u,v), "dot": lambda a,b: sum(x*y for x,y in zip(a,b))})
    return corrected_value,value[1],value[2]

