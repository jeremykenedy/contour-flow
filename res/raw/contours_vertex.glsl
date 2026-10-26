precision highp float;
attribute vec2 aPosition;
uniform float uPhase, uRelief;
uniform vec2 uPixel;
varying highp vec4 vField;

vec3 hill(vec2 p,float falloff) {
    float value=exp(-dot(p,p)*falloff);
    return vec3(value,-2.0*falloff*p*value);
}

void main() {
    vec2 uv=(aPosition+1.)*.5;
    float t=uPhase;
    vec2 size=vec2(4.1,4.1*uPixel.x/uPixel.y);
    vec2 p=(uv-.5)*size;
    p+=vec2(.32*sin(t),.24*cos(t));
    float wx=.23*sin(p.y*1.6+t);
    float wy=.18*sin(p.x*1.3-t);
    vec2 q=p+vec2(wx,wy);
    vec3 north=hill(q+vec2(1.05+.16*sin(t),.18),2.1);
    vec3 center=hill(q-vec2(.22,.34+.18*cos(t)),2.8);
    vec3 east=hill(q-vec2(1.25,-.62),2.3);
    vec3 south=hill(q-vec2(-.88,.78),2.5);
    vec3 bowl=hill(q-vec2(.23,-.24),2.0);
    float a=q.x*2.4+.35*cos(q.y*1.9)+t;
    float b=q.y*2.6-t;
    float wave=sin(a)*cos(b);
    vec2 waveGradient=vec2(2.4*cos(a)*cos(b),
        -.665*sin(q.y*1.9)*cos(a)*cos(b)-2.6*sin(a)*sin(b));
    vec3 h;
    if(uRelief<.5) {
        h=vec3(.39,0.,0.)+.27*north+.24*center+.24*east+.20*south-.10*bowl;
    } else if(uRelief<1.5) {
        h=vec3(.62,0.,0.)-.25*north-.22*center+.15*east-.19*bowl;
    } else {
        h=vec3(.51-q.y*.15,0.,-.15)+.22*north-.13*center+.18*south;
    }
    h+=vec3(wave,waveGradient)*.065;
    vec2 gradient=vec2(h.y+h.z*.234*cos(p.x*1.3-t),
        h.z+h.y*.368*cos(p.y*1.6+t));
    vec2 gradientUV=gradient*size;
    vField=vec4(h.x,gradientUV,dot(gradientUV,uv));
    gl_Position=vec4(aPosition,0.,1.);
}
