precision highp float;
const float MIN_SLOPE=0.0000001;
const float FLAT_SLOPE=0.000003;
uniform float uPhase, uLevels, uWeight, uLight;
uniform vec2 uPixel;
uniform vec3 uLow, uHigh, uInk;
varying highp vec4 vField;

float contourBand(float distance,float footprint,float innerPixels,float outerPixels) {
    float pixelDistance=distance/max(footprint,MIN_SLOPE);
    float visibility=smoothstep(MIN_SLOPE,FLAT_SLOPE,footprint);
    return (1.-smoothstep(innerPixels,outerPixels,pixelDistance))*visibility;
}

void main() {
    vec2 uv=gl_FragCoord.xy*uPixel;
    vec2 size=vec2(4.1,4.1*uPixel.x/uPixel.y);
    float t=uPhase;
    // This gradient correction exactly reconstructs a quadratic field between vertices.
    float value=vField.x+.5*(dot(vField.yz,uv)-vField.w);
    vec3 h=vec3(value,0.,0.);
    vec2 gradient=vField.yz/size;
    vec3 normal=normalize(vec3(-gradient*2.8,1.));
    float diffuse=.38+.62*max(0.,dot(normal,normalize(vec3(-.55,.65,1.))));
    vec3 color=mix(uLow,uHigh,smoothstep(.22,.84,h.x))*diffuse;
    // The full-resolution line footprint keeps the original analytic gradient.
    float footprint=length(gradient*size*uPixel);
    float distance=abs(fract(h.x*uLevels+.5)-.5)/uLevels;
    float contour=contourBand(distance,footprint,uWeight,uWeight+.8);
    float halo=contourBand(distance,footprint,uWeight+1.,uWeight+6.);
    float accent=.65+.35*sin(h.x*8.0+t);
    color+=uInk*halo*.055;
    color=mix(color,uInk*(.70+.22*accent),contour*.87);
    float vignette=1.-.28*dot(uv-.5,uv-.5);
    gl_FragColor=vec4(clamp(color*uLight*vignette,0.,1.),1.);
}
