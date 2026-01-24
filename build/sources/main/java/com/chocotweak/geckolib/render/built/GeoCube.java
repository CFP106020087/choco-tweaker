package com.chocotweak.geckolib.render.built;

import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a cube with 6 quad faces.
 * Backport from YSM GeckoLib 1.20.1.
 * 
 * Supports both Box UV and Per-face UV formats.
 * 
 * Key coordinate transforms (matching YSM exactly):
 * - origin: -(origin.x + size.x)/16, origin.y/16, origin.z/16
 * - rotation: -rotX, -rotY, rotZ (in radians)
 * - pivot: -pivotX, pivotY, pivotZ
 */
public class GeoCube {
        public GeoQuad[] quads = new GeoQuad[6];
        public Vector3f pivot;
        public Vector3f rotation; // In radians
        public Vector3f size = new Vector3f();
        public double inflate;
        public Boolean mirror;

        private GeoCube(double[] size) {
                if (size != null && size.length >= 3) {
                        this.size.set((float) size[0], (float) size[1], (float) size[2]);
                }
        }

        /**
         * Create a GeoCube from parsed Bedrock JSON data.
         * Supports both Box UV and Per-face UV formats.
         */
        public static GeoCube create(double[] originArr, double[] sizeArr, double[] rotationArr,
                        double[] pivotArr, CubeUV cubeUV, float textureWidth, float textureHeight,
                        double inflateValue, Boolean mirrorValue) {

                GeoCube cube = new GeoCube(sizeArr);
                cube.mirror = mirrorValue;
                cube.inflate = inflateValue / 16.0;

                // Parse size
                double sizeX = sizeArr != null && sizeArr.length > 0 ? sizeArr[0] : 0;
                double sizeY = sizeArr != null && sizeArr.length > 1 ? sizeArr[1] : 0;
                double sizeZ = sizeArr != null && sizeArr.length > 2 ? sizeArr[2] : 0;

                // Parse origin and apply coordinate transform (matching YSM)
                double originX = originArr != null && originArr.length > 0 ? originArr[0] : 0;
                double originY = originArr != null && originArr.length > 1 ? originArr[1] : 0;
                double originZ = originArr != null && originArr.length > 2 ? originArr[2] : 0;

                // YSM coordinate transform
                double ox = -(originX + sizeX) / 16.0;
                double oy = originY / 16.0;
                double oz = originZ / 16.0;

                double sx = sizeX / 16.0;
                double sy = sizeY / 16.0;
                double sz = sizeZ / 16.0;

                // Parse rotation and convert to radians with YSM transforms
                double rotX = 0, rotY = 0, rotZ = 0;
                if (rotationArr != null && rotationArr.length >= 3) {
                        rotX = Math.toRadians(-rotationArr[0]); // Negate X
                        rotY = Math.toRadians(-rotationArr[1]); // Negate Y
                        rotZ = Math.toRadians(rotationArr[2]); // Keep Z
                }
                cube.rotation = new Vector3f((float) rotX, (float) rotY, (float) rotZ);

                // Parse pivot with YSM transform
                double pivX = 0, pivY = 0, pivZ = 0;
                if (pivotArr != null && pivotArr.length >= 3) {
                        pivX = -pivotArr[0]; // Negate X
                        pivY = pivotArr[1];
                        pivZ = pivotArr[2];
                }
                cube.pivot = new Vector3f((float) pivX, (float) pivY, (float) pivZ);

                // Create 8 vertices (P1-P8) with inflation
                double inf = cube.inflate;
                GeoVertex P1 = new GeoVertex(ox - inf, oy - inf, oz - inf);
                GeoVertex P2 = new GeoVertex(ox - inf, oy - inf, oz + sz + inf);
                GeoVertex P3 = new GeoVertex(ox - inf, oy + sy + inf, oz - inf);
                GeoVertex P4 = new GeoVertex(ox - inf, oy + sy + inf, oz + sz + inf);
                GeoVertex P5 = new GeoVertex(ox + sx + inf, oy - inf, oz - inf);
                GeoVertex P6 = new GeoVertex(ox + sx + inf, oy - inf, oz + sz + inf);
                GeoVertex P7 = new GeoVertex(ox + sx + inf, oy + sy + inf, oz - inf);
                GeoVertex P8 = new GeoVertex(ox + sx + inf, oy + sy + inf, oz + sz + inf);

                if (cubeUV.isBoxUV) {
                        // Box UV mapping
                        createBoxUVQuads(cube, cubeUV.boxUVCoords, sizeX, sizeY, sizeZ,
                                        textureWidth, textureHeight, mirrorValue,
                                        P1, P2, P3, P4, P5, P6, P7, P8);
                } else {
                        // Per-face UV mapping
                        createPerFaceUVQuads(cube, cubeUV, textureWidth, textureHeight, mirrorValue,
                                        P1, P2, P3, P4, P5, P6, P7, P8);
                }

                return cube;
        }

        private static void createBoxUVQuads(GeoCube cube, double[] uv,
                        double sizeX, double sizeY, double sizeZ,
                        float textureWidth, float textureHeight, Boolean mirrorValue,
                        GeoVertex P1, GeoVertex P2, GeoVertex P3, GeoVertex P4,
                        GeoVertex P5, GeoVertex P6, GeoVertex P7, GeoVertex P8) {

                double u = uv != null && uv.length > 0 ? uv[0] : 0;
                double v = uv != null && uv.length > 1 ? uv[1] : 0;
                double uvSizeX = Math.floor(sizeX);
                double uvSizeY = Math.floor(sizeY);
                double uvSizeZ = Math.floor(sizeZ);

                boolean shouldMirror = Boolean.TRUE.equals(mirrorValue);

                if (!shouldMirror) {
                        // Standard box UV layout
                        cube.quads[0] = new GeoQuad(new GeoVertex[] { P4, P3, P1, P2 },
                                        new double[] { u + uvSizeZ + uvSizeX, v + uvSizeZ },
                                        new double[] { uvSizeZ, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.WEST);
                        cube.quads[1] = new GeoQuad(new GeoVertex[] { P7, P8, P6, P5 },
                                        new double[] { u, v + uvSizeZ },
                                        new double[] { uvSizeZ, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.EAST);
                        cube.quads[2] = new GeoQuad(new GeoVertex[] { P3, P7, P5, P1 },
                                        new double[] { u + uvSizeZ, v + uvSizeZ },
                                        new double[] { uvSizeX, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.NORTH);
                        cube.quads[3] = new GeoQuad(new GeoVertex[] { P8, P4, P2, P6 },
                                        new double[] { u + uvSizeZ + uvSizeX + uvSizeZ, v + uvSizeZ },
                                        new double[] { uvSizeX, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.SOUTH);
                        cube.quads[4] = new GeoQuad(new GeoVertex[] { P4, P8, P7, P3 },
                                        new double[] { u + uvSizeZ, v },
                                        new double[] { uvSizeX, uvSizeZ },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.UP);
                        cube.quads[5] = new GeoQuad(new GeoVertex[] { P1, P5, P6, P2 },
                                        new double[] { u + uvSizeZ + uvSizeX, v + uvSizeZ },
                                        new double[] { uvSizeX, -uvSizeZ },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.DOWN);
                } else {
                        // Mirrored box UV layout
                        cube.quads[0] = new GeoQuad(new GeoVertex[] { P7, P8, P6, P5 },
                                        new double[] { u + uvSizeZ + uvSizeX, v + uvSizeZ },
                                        new double[] { uvSizeZ, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.WEST);
                        cube.quads[1] = new GeoQuad(new GeoVertex[] { P4, P3, P1, P2 },
                                        new double[] { u, v + uvSizeZ },
                                        new double[] { uvSizeZ, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.EAST);
                        cube.quads[2] = new GeoQuad(new GeoVertex[] { P3, P7, P5, P1 },
                                        new double[] { u + uvSizeZ, v + uvSizeZ },
                                        new double[] { uvSizeX, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.NORTH);
                        cube.quads[3] = new GeoQuad(new GeoVertex[] { P8, P4, P2, P6 },
                                        new double[] { u + uvSizeZ + uvSizeX + uvSizeZ, v + uvSizeZ },
                                        new double[] { uvSizeX, uvSizeY },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.SOUTH);
                        cube.quads[4] = new GeoQuad(new GeoVertex[] { P1, P5, P6, P2 },
                                        new double[] { u + uvSizeZ, v },
                                        new double[] { uvSizeX, uvSizeZ },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.UP);
                        cube.quads[5] = new GeoQuad(new GeoVertex[] { P4, P8, P7, P3 },
                                        new double[] { u + uvSizeZ + uvSizeX, v + uvSizeZ },
                                        new double[] { uvSizeX, -uvSizeZ },
                                        textureWidth, textureHeight, mirrorValue, EnumFacing.DOWN);
                }
        }

        private static void createPerFaceUVQuads(GeoCube cube, CubeUV cubeUV,
                        float textureWidth, float textureHeight, Boolean mirrorValue,
                        GeoVertex P1, GeoVertex P2, GeoVertex P3, GeoVertex P4,
                        GeoVertex P5, GeoVertex P6, GeoVertex P7, GeoVertex P8) {

                FaceUV west = cubeUV.west;
                FaceUV east = cubeUV.east;
                FaceUV north = cubeUV.north;
                FaceUV south = cubeUV.south;
                FaceUV up = cubeUV.up;
                FaceUV down = cubeUV.down;

                boolean shouldMirror = Boolean.TRUE.equals(mirrorValue);

                if (!shouldMirror) {
                        // Standard per-face UV
                        cube.quads[0] = west == null ? null
                                        : new GeoQuad(new GeoVertex[] { P4, P3, P1, P2 }, west.getUv(),
                                                        west.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.WEST);
                        cube.quads[1] = east == null ? null
                                        : new GeoQuad(new GeoVertex[] { P7, P8, P6, P5 }, east.getUv(),
                                                        east.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.EAST);
                        cube.quads[2] = north == null ? null
                                        : new GeoQuad(new GeoVertex[] { P3, P7, P5, P1 }, north.getUv(),
                                                        north.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.NORTH);
                        cube.quads[3] = south == null ? null
                                        : new GeoQuad(new GeoVertex[] { P8, P4, P2, P6 }, south.getUv(),
                                                        south.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.SOUTH);
                        cube.quads[4] = up == null ? null
                                        : new GeoQuad(new GeoVertex[] { P4, P8, P7, P3 }, up.getUv(), up.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.UP);
                        cube.quads[5] = down == null ? null
                                        : new GeoQuad(new GeoVertex[] { P1, P5, P6, P2 }, down.getUv(),
                                                        down.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.DOWN);
                } else {
                        // Mirrored per-face UV (swap east/west and up/down vertices)
                        cube.quads[0] = west == null ? null
                                        : new GeoQuad(new GeoVertex[] { P7, P8, P6, P5 }, west.getUv(),
                                                        west.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.WEST);
                        cube.quads[1] = east == null ? null
                                        : new GeoQuad(new GeoVertex[] { P4, P3, P1, P2 }, east.getUv(),
                                                        east.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.EAST);
                        cube.quads[2] = north == null ? null
                                        : new GeoQuad(new GeoVertex[] { P3, P7, P5, P1 }, north.getUv(),
                                                        north.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.NORTH);
                        cube.quads[3] = south == null ? null
                                        : new GeoQuad(new GeoVertex[] { P8, P4, P2, P6 }, south.getUv(),
                                                        south.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.SOUTH);
                        cube.quads[4] = up == null ? null
                                        : new GeoQuad(new GeoVertex[] { P1, P5, P6, P2 }, up.getUv(), up.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.UP);
                        cube.quads[5] = down == null ? null
                                        : new GeoQuad(new GeoVertex[] { P4, P8, P7, P3 }, down.getUv(),
                                                        down.getUvSize(),
                                                        textureWidth, textureHeight, mirrorValue, EnumFacing.DOWN);
                }
        }
}
