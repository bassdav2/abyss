"""Reproduzierbare, orthografische Figuren-Assets für ABYSS (Blender 5.1).

Aufruf: Blender --background --python tools/render_actors.py
Native .blend-Quellen und einzelne transparente PNG-Frames bleiben editierbar.
"""
import bpy
import math
import json
import sys
from pathlib import Path
from mathutils import Vector

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/art/actors"
SOURCE = ROOT / "art-source"
OUT.mkdir(parents=True, exist_ok=True)
SOURCE.mkdir(exist_ok=True)


def material(name, color, metallic=0.0, rough=.5, emission=0.0):
    mat = bpy.data.materials.get(name) or bpy.data.materials.new(name)
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    bsdf = nodes.get("Principled BSDF")
    bsdf.inputs["Base Color"].default_value = (*color, 1)
    bsdf.inputs["Metallic"].default_value = metallic
    bsdf.inputs["Roughness"].default_value = rough
    if emission:
        bsdf.inputs["Emission Color"].default_value = (*color, 1)
        bsdf.inputs["Emission Strength"].default_value = emission
    else:
        noise = nodes.new("ShaderNodeTexNoise")
        noise.inputs["Scale"].default_value = 58
        noise.inputs["Detail"].default_value = 3
        bump = nodes.new("ShaderNodeBump")
        bump.inputs["Strength"].default_value = .16
        bump.inputs["Distance"].default_value = .025
        mat.node_tree.links.new(noise.outputs["Fac"], bump.inputs["Height"])
        mat.node_tree.links.new(bump.outputs["Normal"], bsdf.inputs["Normal"])
        patina = nodes.new("ShaderNodeTexNoise")
        patina.inputs["Scale"].default_value = 13
        patina.inputs["Detail"].default_value = 5
        tones = nodes.new("ShaderNodeValToRGB")
        tones.color_ramp.elements[0].position = .22
        tones.color_ramp.elements[0].color = (*(max(.003,c * .35) for c in color),1)
        tones.color_ramp.elements[1].position = .78
        tones.color_ramp.elements[1].color = (*(min(.95,c * 1.27) for c in color),1)
        mat.node_tree.links.new(patina.outputs["Fac"], tones.inputs[0])
        mat.node_tree.links.new(tones.outputs["Color"], bsdf.inputs["Base Color"])
    return mat


def box(name, position, size, mat, bevel=.045):
    bpy.ops.mesh.primitive_cube_add(size=1, location=position)
    ob = bpy.context.object
    ob.name = name
    ob.dimensions = size
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    if bevel:
        mod = ob.modifiers.new("Worn soft edges", "BEVEL")
        mod.width = bevel
        mod.segments = 3
        ob.modifiers.new("Weighted normals", "WEIGHTED_NORMAL")
    ob.data.materials.append(mat)
    return ob


def ball(name, position, scale, mat):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=24, ring_count=12, location=position)
    ob = bpy.context.object
    ob.name = name
    ob.scale = scale
    ob.data.materials.append(mat)
    for polygon in ob.data.polygons:
        polygon.use_smooth = True
    return ob


def link(name, start, end, radius, mat, vertices=16):
    start, end = Vector(start), Vector(end)
    delta = end - start
    bpy.ops.mesh.primitive_cylinder_add(vertices=vertices, radius=radius, depth=delta.length, location=(start + end) / 2)
    ob = bpy.context.object
    ob.name = name
    ob.rotation_euler = delta.to_track_quat("Z", "Y").to_euler()
    ob.data.materials.append(mat)
    bevel = ob.modifiers.new("Machined edges", "BEVEL")
    bevel.width = min(.025, radius * .2)
    bevel.segments = 2
    for polygon in ob.data.polygons:
        polygon.use_smooth = True
    return ob


def ring(name, position, major, minor, mat, rotation=(math.pi / 2, 0, 0)):
    bpy.ops.mesh.primitive_torus_add(major_segments=32, minor_segments=8, location=position,
                                   major_radius=major, minor_radius=minor, rotation=rotation)
    ob = bpy.context.object
    ob.name = name
    ob.data.materials.append(mat)
    return ob


M = {}


def init_scene(scale=2.6):
    bpy.ops.object.select_all(action="SELECT")
    bpy.ops.object.delete(use_global=False)
    scene = bpy.context.scene
    scene.render.engine = "BLENDER_EEVEE"
    scene.render.resolution_x = 256
    scene.render.resolution_y = 256
    scene.render.resolution_percentage = 100
    scene.render.film_transparent = True
    scene.render.image_settings.file_format = "PNG"
    scene.render.image_settings.color_mode = "RGBA"
    scene.render.image_settings.color_depth = "8"
    scene.render.image_settings.compression = 30
    scene.world.color = (.10, .13, .16)
    scene.view_settings.view_transform = "AgX"
    scene.view_settings.look = "AgX - Medium High Contrast"
    floor_anchor = .944
    center_z = (floor_anchor - .5) * scale
    bpy.ops.object.camera_add(location=(0, -12, center_z))
    camera = bpy.context.object
    camera.rotation_euler = (Vector((0, 0, center_z)) - camera.location).to_track_quat("-Z", "Y").to_euler()
    camera.data.type = "ORTHO"
    camera.data.ortho_scale = scale
    scene.camera = camera
    for name, pos, power, color, size in [
        ("Warm service light", (-3, -4, 5), 750, (1.0, .64, .29), 4),
        ("Cold window rim", (2, 3, 3.5), 1000, (.2, .65, 1), 3),
        ("Face fill", (3, -3, 2), 320, (.64, .82, 1), 3),
    ]:
        bpy.ops.object.light_add(type="AREA", location=pos)
        light = bpy.context.object
        light.name = name
        light.data.energy = power
        light.data.color = color
        light.data.shape = "DISK"
        light.data.size = size
        light.rotation_euler = (Vector((0, 0, 1)) - light.location).to_track_quat("-Z", "Y").to_euler()
    for key, params in {
        "yellow": ((.46, .23, .038), .0, .86, 0),
        "cloth": ((.055, .075, .085), .0, .83, 0),
        "boot": ((.025, .035, .04), .15, .65, 0),
        "steel": ((.18, .23, .25), .8, .35, 0),
        "dark": ((.028, .05, .061), .8, .4, 0),
        "copper": ((.35, .125, .04), .75, .4, 0),
        "teal": ((.018, .23, .24), .7, .34, 0),
        "cyan": ((.04, .75, .9), .2, .2, 3),
        "red": ((.9, .06, .009), .2, .25, 3),
        "white": ((.9, .94, 1), .1, .2, 5),
        "visor": ((.012, .035, .041), .65, .17, 0),
    }.items():
        if key not in M:
            M[key] = material(key, *params)


def human(pose, frame, count):
    t = frame / count * math.tau
    walking = pose == "walk"
    bob = math.sin(t * 2) * .025 if walking else math.sin(t) * .012
    lean = .12 if pose == "dash" else -.1 if pose == "hurt" else .04
    hip = Vector((0, 0, .90 + bob))
    torso = box("Yellow weatherproof jacket", (lean, 0, 1.31 + bob), (.48, .40, .65), M["yellow"], .155)
    torso.rotation_euler.y = -.13 if pose == "dash" else .04
    box("Dark waist belt", (0, 0, .99 + bob), (.47, .42, .12), M["boot"], .025)
    box("Belt buckle", (.17, -.224, 1.00 + bob), (.1, .035, .08), M["steel"], .008)
    box("Jacket pocket", (.07, -.213, 1.24 + bob), (.22, .035, .19), M["yellow"], .027)
    link("Pocket stitched rim", (-.025, -.240, 1.325 + bob), (.168, -.240, 1.325 + bob), .009, M["cloth"])
    for i in range(4):
        fold_z=1.06+i*.12+bob
        link("Jacket seam", (-.11,-.205,fold_z),(.18,-.213,fold_z+.025),.008,M["copper"])
    for side in [1, -1]:
        y = .13 * side
        swing = math.sin(t + (math.pi if side == 1 else 0)) * .46 if walking else (.43 if pose == "dash" else .07 * side)
        knee = hip + Vector((swing * .58, y, -.37))
        foot_x = swing
        foot_z = .10 + (max(0, math.cos(t + (math.pi if side == 1 else 0))) * .12 if walking else 0)
        ankle = Vector((foot_x, y, foot_z))
        link("Trouser thigh", hip + Vector((0, y, 0)), knee, .125, M["cloth"])
        ball("Kneepad", knee + Vector((.045, -.04, 0)), (.13, .135, .145), M["boot"])
        link("Trouser calf", knee, ankle, .105, M["cloth"])
        box("Work boot", (foot_x + .09, y - .01, foot_z - .015), (.38, .24, .19), M["boot"], .045)
        box("Steel toe", (foot_x + .235, y - .018, foot_z), (.10, .24, .12), M["dark"], .035)
    # Wiedererkennbare Doppel-Flaschen und feste Materialzuordnung.
    for y in [-.13, .13]:
        link("Teal air tank", (-.30, y, 1.09 + bob), (-.30, y, 1.65 + bob), .13, M["teal"])
        ring("Tank retention band", (-.30, y, 1.23 + bob), .132, .023, M["steel"], (0, 0, 0))
        ring("Tank retention band", (-.30, y, 1.51 + bob), .132, .023, M["steel"], (0, 0, 0))
        ball("Tank cap", (-.30, y, 1.67 + bob), (.07, .07, .06), M["steel"])
    link("Shoulder strap", (-.12, -.235, 1.65 + bob), (.12, -.235, 1.05 + bob), .035, M["boot"])
    ball("Helmet", (.06 + lean, 0, 1.86 + bob), (.235, .22, .22), M["dark"])
    ball("Face visor", (.25 + lean, -.04, 1.85 + bob), (.11, .205, .135), M["visor"])
    box("Helmet rim", (.08 + lean, 0, 1.94 + bob), (.53, .48, .06), M["yellow"], .02)
    ball("Headlamp housing", (.30 + lean, -.15, 1.99 + bob), (.10, .09, .085), M["dark"])
    ball("Headlamp", (.367 + lean, -.17, 1.996 + bob), (.024, .065, .058), M["white"])
    link("Breathing hose", (.20 + lean, .03, 1.72 + bob), (-.29, .03, 1.4 + bob), .037, M["boot"])
    ball("Respirator", (.29+lean,-.08,1.725+bob),(.07,.10,.065),M["steel"])
    ring("Respirator grille",(.34+lean,-.11,1.725+bob),.04,.012,M["dark"],(0,math.pi/2,0))
    for side in [1, -1]:
        shoulder = Vector((.025 + lean, side * .23, 1.55 + bob))
        if pose == "attack" and side == -1:
            angle = -1.65 + frame / max(1, count - 1) * 2.65
            elbow = shoulder + Vector((.32 * math.sin(angle), -.02, -.30 * math.cos(angle)))
            hand = elbow + Vector((.32 * math.sin(angle + .35), 0, -.30 * math.cos(angle + .35)))
        else:
            arm_swing = -.25 * math.sin(t + (math.pi if side == 1 else 0)) if walking else .12
            elbow = shoulder + Vector((arm_swing, 0, -.28))
            hand = elbow + Vector((.23, -.02, -.18))
        link("Jacket upper sleeve", shoulder, elbow, .105, M["yellow"])
        link("Jacket forearm", elbow, hand, .088, M["yellow"])
        ball("Work glove", hand, (.095, .105, .10), M["boot"])
        if side == -1:
            tool_end = hand + Vector((.17 if pose != "attack" else .35, -.02, -.24 if pose != "attack" else .05))
            link("Impact wrench handle", hand, tool_end, .05, M["steel"])
            box("Impact wrench head", tool_end, (.20, .16, .15), M["dark"], .035)
            box("Tool energy cell", tool_end + Vector((.03, -.08, 0)), (.10, .045, .065), M["cyan"], .012)


def robot(kind, pose, frame, count):
    t = frame / count * math.tau
    glow = M["red"]
    attack = pose == "attack"
    if kind == "drone":
        z = 1.10 + math.sin(t) * .055
        ball("Armoured drone shell", (0, 0, z), (.48, .34, .33), M["dark"])
        ring("Front sensor ring", (.31, -.19, z + .01), .15, .038, M["steel"], (0, math.pi / 2, 0))
        ball("Sensor", (.405, -.16, z + .015), (.07, .12, .12), glow)
        for side in [-1, 1]:
            link("Fan boom", (-.12, 0, z), (-.43, side * .5, z + .06), .055, M["steel"])
            ring("Stabilizer", (-.43, side * .5, z + .06), .27, .065, M["teal"], (0, 0, 0))
            box("Rotor", (-.43, side * .5, z + .06), (.46, .035, .025), M["steel"], .005).rotation_euler.z = t * 5
        link("Cannon", (.2, -.07, z - .18), (.60, -.07, z - .18), .07, M["steel"])
        ball("Engine glow", (-.40, 0, z - .18), (.13, .19, .11), M["cyan"])
        return
    if kind == "scuttler":
        z=.77+(math.sin(t*2)*.025 if pose=="walk" else 0)
        ball("Low riveted carapace",(0,0,z),(.48,.32,.28),M["copper"])
        box("Dorsal armor ridge",(-.12,0,z+.22),(.48,.37,.10),M["dark"],.045)
        for i in range(4):
            link("Cooling fin",(-.30+i*.11,-.24,z+.12),(-.30+i*.11,.24,z+.12),.018,M["steel"])
        for side in [-1,1]:
            for fore in [-1,1]:
                offset=t+(math.pi if side*fore>0 else 0)
                stride=math.sin(offset)*.13 if pose=="walk" else 0
                lift=max(0,math.cos(offset))*.11 if pose=="walk" else 0
                base=Vector((fore*.25,side*.24,z-.07))
                knee=Vector((fore*(.48+stride),side*.36,.43))
                foot=Vector((fore*(.56-stride),side*.41,.08+lift))
                link("Crab drive upper strut",base,knee,.064,M["steel"])
                ball("Exposed leg pivot",knee,(.094,.09,.094),M["dark"])
                link("Crab drive piston",knee,foot,.045,M["copper"])
                box("Magnetic claw",foot,(.20,.14,.09),M["dark"],.025)
        link("Armored sensor stem",(.35,0,z),(.50,0,z+.02),.16,M["dark"])
        ball("Hostile optical sensor",(.53,-.10,z+.025),(.042,.13,.085),M["red"])
        for side in [-1,1]:
            tip=Vector((.75 if attack else .57,side*.19,z-.24))
            link("Breach claw",(.30,side*.20,z-.17),tip,.045,M["steel"])
            link("Breach claw hook",tip,tip+Vector((.04,-side*.075,-.09)),.03,M["dark"])
        return
    boss = kind == "captain"
    sentinel = kind == "sentinel"
    s = 1.6 if boss else 1.1 if sentinel else .88
    body_z = 1.19 * s + (math.sin(t * 2) * .04 if pose == "walk" else 0)
    armor = M["teal"] if boss else M["yellow"] if sentinel else M["copper"]
    box("Armoured chassis", (0, 0, body_z), (.66 * s, .64 * s, .63 * s), armor, .08)
    box("Central armored plate", (.15 * s, -.35 * s, body_z), (.52 * s, .08 * s, .48 * s), M["dark"], .04)
    ring("Sensor ring", (.40 * s, -.08 * s, body_z + .09 * s), .13 * s, .035 * s, M["steel"], (0, math.pi / 2, 0))
    ball("Crimson sensor", (.47 * s, -.10 * s, body_z + .09 * s), (.045 * s, .11 * s, .11 * s), glow)
    for i in range(3):
        box("Armor rivet", ((-.2 + i * .19) * s, -.40 * s, body_z + .2 * s), (.045 * s, .025 * s, .045 * s), M["steel"], .008)
    for side in [1, -1]:
        hip = Vector((-.10 * s, side * .25 * s, body_z - .28 * s))
        swing = math.sin(t + (math.pi if side == 1 else 0)) * .24 if pose == "walk" else .1 * side
        knee = Vector(((-.08 + swing) * s, side * .28 * s, .45 * s))
        ankle = Vector(((.05 - swing) * s, side * .28 * s, .12 * s))
        link("Hydraulic thigh", hip, knee, .08 * s, M["steel"])
        ball("Knee joint", knee, (.14 * s,) * 3, M["dark"])
        link("Actuator", knee, ankle, .10 * s, armor)
        box("Magnetic foot", ankle + Vector((.07 * s, 0, -.035 * s)), (.42 * s, .27 * s, .16 * s), M["dark"], .035)
        shoulder = Vector((0, side * .39 * s, body_z + .08 * s))
        extension = .4 if attack else .0
        elbow = shoulder + Vector(((.25 + extension) * s, 0, -.26 * s))
        wrist = elbow + Vector((.28 * s, 0, -.30 * s))
        link("Manipulator arm", shoulder, elbow, .065 * s, M["steel"])
        ball("Manipulator elbow", elbow, (.13 * s,) * 3, armor)
        link("Manipulator forearm", elbow, wrist, .065 * s, M["steel"])
        if side == -1:
            box("Heavy tool", wrist, (.25 * s, .25 * s, .33 * s), M["dark"], .035)
            ring("Tool ignition", wrist + Vector((.08 * s, -.13 * s, 0)), .075 * s, .025 * s, glow)
    # Druckhelm, Schutzplatten und echte mechanische Details geben den beiden
    # schweren Gegnern eine erkennbare Silhouette statt eines vergrösserten Würfels.
    head_z=body_z+.62*s
    ball("Pressure helmet",(.02*s,0,head_z),(.30*s,.29*s,.31*s),M["dark"])
    ring("Helmet reinforcement",(.02*s,0,head_z),.305*s,.035*s,armor,(math.pi/2,0,0))
    ball("Forward visor housing",(.26*s,-.075*s,head_z),(.12*s,.22*s,.19*s),M["steel"])
    ball("Red viewport",(.345*s,-.09*s,head_z),(.04*s,.175*s,.135*s),glow)
    link("Visor brow",(.35*s,-.22*s,head_z+.12*s),(.35*s,.1*s,head_z+.12*s),.035*s,armor)
    for side in [-1,1]:
        box("Layered shoulder armor",(-.01*s,side*.40*s,body_z+.22*s),(.54*s,.21*s,.25*s),armor,.06)
        for i in range(3):
            ball("Shoulder fastener",((-.17+i*.16)*s,side*.515*s,body_z+.28*s),(.025*s,)*3,M["steel"])
        link("Rear pressure vessel",(-.40*s,side*.19*s,body_z-.15*s),(-.40*s,side*.19*s,body_z+.38*s),.11*s,M["steel"])
        ring("Vessel band",(-.40*s,side*.19*s,body_z+.13*s),.114*s,.018*s,M["copper"],(0,0,0))
        link("Helmet supply line",(-.34*s,side*.21*s,body_z+.39*s),(-.10*s,side*.21*s,head_z-.17*s),.032*s,M["boot"])
    ring("Chest pressure gauge",(.1*s,-.397*s,body_z-.035*s),.104*s,.025*s,M["copper"])
    ball("Gauge black face",(.1*s,-.401*s,body_z-.035*s),(.084*s,.008*s,.084*s),M["visor"])
    link("Gauge needle",(.1*s,-.414*s,body_z-.035*s),(.14*s,-.414*s,body_z+.015*s),.009*s,M["white"])
    if sentinel:
        box("Riot pressure plate",(.55*s,-.51*s,body_z-.48*s),(.40*s,.10*s,.68*s),M["dark"],.045)
        for z in [-.68,-.46,-.24]:
            box("Shield warning stripe",(.55*s,-.57*s,body_z+z*s),(.34*s,.012*s,.045*s),M["yellow"],.003)
    if boss:
        # Breiter Schulterkragen, zentrale cyanfarbene Energiekammer und ein Rammdorn.
        box("Bridge command mantle",(-.12,-.15,body_z+.50),(.90,.99,.17),M["copper"],.07)
        ring("Core housing",(.17,-.65,body_z-.01),.19,.06,M["steel"])
        ball("Exposed energy core",(.17,-.695,body_z-.01),(.14,.027,.14),M["cyan"])
        link("Powered breaching lance",(.68,-.66,body_z-.55),(1.14,-.66,body_z-.28),.10,M["steel"])
        ring("Lance discharge coil",(.93,-.66,body_z-.40),.145,.028,M["copper"],(0,math.pi/3,0))
        for x in [-.28, -.08, .12]:
            link("Rear radiator", (x * s, .15, body_z + .26 * s), (x * s, .15, body_z + .58 * s), .065, M["copper"])
        box("Bridge authority crest", (-.12, -.57, body_z + .2), (.24, .05, .10), M["cyan"], .01)


def boss_machine(kind, pose, frame, count):
    t=frame/count*math.tau
    attack=pose=="attack"
    bob=math.sin(t)*.025
    if kind=="warden":
        # Unverwechselbare niedrige Raupen, breiter Druckpanzer und Rammschild.
        for side in [-1,1]:
            for i in range(5):
                x=-.68+i*.30
                ball("Track roller",(x,side*.38,.22),(.22,.12,.22),M["dark"])
                ring("Roller hub",(x,side*.51,.22),.12,.022,M["copper"])
            box("Tracked running gear",(-.05,side*.38,.11),(1.70,.27,.17),M["boot"],.06)
            for i in range(11):
                x=-.78+i*.145
                box("Track shoe",(x,side*.38,.05),(.095,.33,.085),M["steel"],.012)
        ball("Pressure iron shell",(-.08,0,1.05+bob),(.69,.48,.72),M["copper"])
        box("Shell shoulder armor",(-.12,0,1.47+bob),(1.39,1.08,.23),M["dark"],.09)
        ball("Observation dome",(.14,0,1.91+bob),(.33,.30,.30),M["steel"])
        ball("Armored visor",(.43,-.10,1.92+bob),(.085,.245,.15),M["red"])
        for i in range(3):
            link("Exhaust stack",(-.63+i*.16,.17,1.52),(-.63+i*.16,.17,1.91),.07,M["dark"])
        extension=.18*math.sin(t) if attack else 0
        link("Ram piston",(.4,-.35,.89),(1.00+extension,-.35,.80),.13,M["steel"])
        box("Massive breach shield",(1.04+extension,-.30,.92),(.21,.90,1.41),M["dark"],.08)
        for z in [.42,.72,1.02,1.32]:
            box("Warning armor",(1.16+extension,-.32,z),(.04,.86,.11),M["yellow"],.02)
        ring("Exposed hydraulic gauge",(-.08,-.50,1.15),.24,.04,M["steel"])
        ball("Warm pressure glass",(-.08,-.535,1.15),(.185,.020,.185),M["red"])
    else:
        # Reaktorspinne: sechs bewegliche Stützen statt humanoider Beine.
        core_z=1.38+bob
        for side in [-1,1]:
            for i in range(3):
                phase=t+i*2.1+side
                x=-.55+i*.52
                foot_x=x+(-.40 if i==0 else .40 if i==2 else .12)
                lift=max(0,math.sin(phase))*.13 if pose=="walk" else 0
                knee=(foot_x,side*.65,.60+lift)
                link("Spider upper strut",(x,side*.25,1.00),knee,.07,M["steel"])
                ball("Hydraulic knuckle",knee,(.14,)*3,M["copper"])
                link("Spider lower strut",knee,(foot_x+.14,side*.82,.10+lift),.06,M["steel"])
                box("Magnetic claw",(foot_x+.20,side*.82,.08+lift),(.34,.18,.12),M["dark"],.035)
        ball("Reactor containment body",(0,0,core_z),(.72,.42,.68),M["dark"])
        ring("Main reactor collar",(0,-.39,core_z),.52,.10,M["copper"])
        ball("Luminous reactor sphere",(0,-.49,core_z),(.38,.10,.38),M["cyan"])
        ring("Core retainer",(0,-.57,core_z),.33,.035,M["steel"])
        for i in range(8):
            angle=i*math.tau/8+(t*.12 if attack else 0)
            x=math.cos(angle)*.62;z=core_z+math.sin(angle)*.62
            box("Segmented heat armor",(x,-.14,z),(.23,.52,.22),M["steel"],.035)
        for x in [-.5,.5]:
            link("Arc cannon barrel",(x,-.1,1.8),(x,-.1,2.27),.095,M["dark"])
            ball("Arc emitter",(x,-.1,2.28),(.12,.12,.09),M["red"] if attack else M["cyan"])
        for i in range(3):
            link("Cooling conduit",(-.45+i*.44,.36,1.12),(-.45+i*.44,.36,1.84),.07,M["teal"])


def main():
    counts = {"idle": 4, "walk": 8, "attack": 6, "dash": 2, "hurt": 2}
    manifest = {"format": 1, "frame_size": 256, "anchor": [.5, .944], "actors": {}}
    wanted=set(sys.argv[sys.argv.index("--")+1:]) if "--" in sys.argv else set()
    for kind in ["player", "scuttler", "drone", "sentinel", "captain", "warden", "reactor"]:
        if wanted and kind not in wanted: continue
        folder = OUT / kind
        folder.mkdir(exist_ok=True)
        poses = counts if kind == "player" else {"idle": 4, "walk": 6, "attack": 4, "hurt": 2}
        manifest["actors"][kind] = poses
        for pose, count in poses.items():
            for frame in range(count):
                scale = 3.6 if kind in ("captain","warden","reactor") else 2.6
                init_scene(scale)
                if kind == "player":
                    human(pose, frame, count)
                elif kind in ("warden","reactor"):
                    boss_machine(kind, pose, frame, count)
                else:
                    robot(kind, pose, frame, count)
                scene = bpy.context.scene
                scene.render.filepath = str(folder / f"{pose}-{frame:02d}.png")
                if pose == "idle" and frame == 0:
                    bpy.ops.wm.save_as_mainfile(filepath=str(SOURCE / f"{kind}.blend"))
                bpy.ops.render.render(write_still=True)
                print(f"ABYSS_RENDER {kind} {pose} {frame + 1}/{count}", flush=True)
    if wanted and (OUT/"manifest.json").exists():
        previous=json.loads((OUT/"manifest.json").read_text())
        previous["actors"].update(manifest["actors"])
        manifest=previous
    (OUT / "manifest.json").write_text(json.dumps(manifest, indent=2))
    print("ABYSS_RENDER_COMPLETE", flush=True)


main()
