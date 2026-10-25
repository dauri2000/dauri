"""Adapt Dalinnar's supplied Java litematic to a Bedrock flushing circuit.

Requires nbtlib and numpy. Keeps the original data version so legacy Items can be
upgraded by the reader. Does not pretend to simulate Minecraft redstone or mobs.
"""
from pathlib import Path
from collections import Counter
import copy, hashlib, json, math, sys, time
import nbtlib
from nbtlib import Compound, List, String, Int, Long, Byte, LongArray

FLOORS = list(range(34, 71, 3))
CX = CZ = 22

def decode(region):
    sx, sy, sz = [abs(int(region['Size'][k])) for k in 'xyz']
    bits = max(2, (len(region['BlockStatePalette']) - 1).bit_length())
    mask = (1 << bits) - 1
    words = [int(v) & ((1 << 64) - 1) for v in region['BlockStates']]
    out = []
    for i in range(sx * sy * sz):
        bit = i * bits; word, offset = divmod(bit, 64)
        v = words[word] >> offset
        if offset + bits > 64:
            v |= words[word + 1] << (64 - offset)
        out.append(v & mask)
    return (sx, sy, sz), out

def encode(blocks, palette_length):
    bits = max(2, (palette_length - 1).bit_length())
    words = [0] * ((len(blocks) * bits + 63) // 64)
    for i, value in enumerate(blocks):
        bit = i * bits; word, offset = divmod(bit, 64)
        words[word] |= value << offset
        if offset + bits > 64:
            words[word + 1] |= value >> (64 - offset)
    limit = (1 << 64) - 1
    return LongArray([(v & limit) if (v & limit) < (1 << 63) else (v & limit) - (1 << 64) for v in words])

def state(name, **props):
    out = Compound({'Name': String('minecraft:' + name)})
    if props: out['Properties'] = Compound({k: String(v) for k, v in props.items()})
    return out

def adapt(source, outdir):
    outdir = Path(outdir); outdir.mkdir(parents=True, exist_ok=True)
    original = nbtlib.load(source)
    f = copy.deepcopy(original)
    region = next(iter(f['Regions'].values()))
    old_region = next(iter(original['Regions'].values()))
    old_dims, old = decode(old_region)
    assert old_dims == (45, 77, 45)
    # Same normalized placement origin and X/Z bounds; extend upward for AFK.
    sx, sy, sz = 45, 103, 45
    blocks = old + [0] * (sx * sz * (sy - old_dims[1]))
    palette = region['BlockStatePalette']
    edits = []
    def idx(x,y,z): return (y * sz + z) * sx + x
    def get(x,y,z): return int(blocks[idx(x,y,z)])
    def add(s):
        for i, existing in enumerate(palette):
            if existing == s: return i
        palette.append(s); return len(palette)-1
    def put(x,y,z,value,reason):
        if not isinstance(value, int): value = add(value)
        pos = idx(x,y,z); before = blocks[pos]
        if before != value:
            edits.append({'position':[x,y,z], 'from':before, 'to':value, 'reason':reason})
            blocks[pos] = value
    air, stone = 0, 2
    bottom = 1
    dust = add(state('redstone_wire', power='0', north='none', south='none', east='none', west='none'))
    dispenser = add(state('dispenser', facing='west', triggered='false'))
    observer_up = 22
    observer_west = add(state('observer', facing='west', powered='false'))
    glass = add(state('glass'))
    lever_west = add(state('lever', face='wall', facing='west', powered='true'))
    # Empty, dry flushing platforms: retain the collector's 64 permanent sources.
    for floor in FLOORS:
        for z in range(15,30):
            for x in range(15,30):
                if abs(x-CX)+abs(z-CZ) <= 7 and str(palette[get(x,floor+1,z)]['Name']) == 'minecraft:water':
                    put(x,floor+1,z,air,'dry_start_flush_water')
        put(22,floor,22,stone,'replace_old_upward_dispenser_with_floor')
        put(22,floor+2,22,air,'remove_java_quasi_connectivity_observer')
        put(23,floor,22,dust,'new_direct_power_column_dust')
        put(23,floor+1,22,dispenser,'dispenser_west_keeps_source_at_original_center')
        put(23,floor+2,22,observer_up,'observer_reads_upper_dust_and_powers_dispenser_directly')
        for x,z in [(24,22),(23,21),(23,23)]:
            put(x,floor,z,stone,'seal_dust_cell_against_water')
        for x in [28,29]:
            put(x,floor,22,air,'remove_two_dry_perch_cells_beyond_seven_step_water_reach')
    put(23,33,22,stone,'support_bottom_dust')
    # Opaque roof, with the original roof slab material reused as a bottom-slab cap.
    # This guarantees darkness without relying on edition-specific slab opacity.
    roof_slab_positions = [(x,z) for z in range(sz) for x in range(sx)
                           if old[idx(x,73,z)] == 1]
    for x,z in roof_slab_positions:
        put(x,73,z,stone,'opaque_roof_same_footprint')
    put(22,73,22,stone,'remove_old_clock_observer_and_close_roof')
    # Comparator -> side observer -> three dust cells -> top column observer.
    put(23,74,22,observer_west,'clock_tap_reads_existing_comparator')
    put(24,74,22,stone,'clock_tap_power_conductor')
    put(24,73,23,stone,'support_upper_clock_dust')
    put(24,74,23,dust,'clock_signal_upper_step')
    put(23,72,23,stone,'support_lower_clock_dust')
    put(23,73,23,dust,'clock_signal_lower_step')
    put(23,73,22,dust,'clock_signal_top_column_input')
    put(23,74,23,air,'clear_clock_dust_descent_and_relocate_lever')
    put(21,74,23,lever_west,'same_lock_lever_on_other_side_of_clock_support')
    put(23,75,22,bottom,'spawnproof_clock_observer_top')
    put(24,75,22,bottom,'spawnproof_clock_conductor_top')
    keep_clear = {(23,23),(23,22),(24,22),(24,23)}
    for x,z in roof_slab_positions:
        if get(x,74,z) == air and (x,z) not in keep_clear:
            put(x,74,z,bottom,'roof_spawnproof_cap_reuses_slab_material')
    # Collector and storage retain the original drop and 5x5 hopper arrangement.
    gate = copy.deepcopy(palette[get(21,32,20)])
    gate['Properties']['open'] = String('true')
    put(21,32,20,gate,'open_single_accidentally_closed_collector_gate')
    put(25,0,22,state('chest', facing='north', type='left', waterlogged='false'),'storage_at_existing_hopper_output')
    put(26,0,22,state('chest', facing='north', type='right', waterlogged='false'),'storage_at_existing_hopper_output')
    # Covered 3x3 AFK pad: stand at center feet Y=100, within 24..128 of the farm.
    for z in range(21,24):
        for x in range(21,24):
            put(x,99,z,glass,'afk_pad_for_simulation_distance_six_or_more')
            put(x,102,z,bottom,'afk_pad_shelter_and_spawnproof_roof')
    # Inventories are intentional: one WATER bucket per flushing dispenser,
    # exactly 16 ordinary stackable items in the original timer; empty storage.
    tiles = []
    for old_tile in region['TileEntities']:
        t = copy.deepcopy(old_tile)
        xyz = tuple(int(t[k]) for k in 'xyz')
        if xyz[0] == 22 and xyz[2] == 22 and xyz[1] in FLOORS:
            t['x'] = Int(23); t['y'] = Int(xyz[1]+1)
            t['Items'] = List[Compound]([Compound({'Slot':Byte(0),'id':String('minecraft:water_bucket'),'Count':Byte(1)})])
        elif xyz == (22,74,20):
            t['Items'] = List[Compound]([Compound({'Slot':Byte(0),'id':String('minecraft:cobblestone'),'Count':Byte(16)})])
            t['TransferCooldown'] = Int(0)
        elif xyz == (22,74,21) or xyz[1] == 0:
            t['Items'] = List[Compound]([])
            if 'TransferCooldown' in t: t['TransferCooldown'] = Int(0)
        tiles.append(t)
    for x in [25,26]:
        tiles.append(Compound({'id':String('minecraft:chest'),'x':Int(x),'y':Int(0),'z':Int(22),'Items':List[Compound]([])}))
    region['TileEntities'] = List[Compound](tiles)
    # Keep negative region axes as supplied, normalize minimum corner to (0,0,0).
    region['Size']['y'] = Int(-sy)
    region['Position']['y'] = Int(sy-1)
    region['BlockStates'] = encode(blocks,len(palette))
    md = f['Metadata']
    md['Name'] = String('early game mob-farm - Bedrock SD6')
    md['Description'] = String('Original: Dalinnar. Bedrock adaptation: direct O-D-dust column, dry start, opaque capped roof, storage and AFK pad. Simulation distance >=6. Stand at local (22.5,100,22.5). See accompanying Spanish guide; not live-tested in Bedrock.')
    md['TimeModified'] = Long(int(time.time()*1000))
    md['TotalVolume'] = Int(len(blocks))
    md['TotalBlocks'] = Int(sum(v != 0 for v in blocks))
    md['EnclosingSize']['y'] = Int(sy)
    target = outdir/'early_game_mob-farm_Bedrock_SD6.litematic'
    f.save(target, gzipped=True)
    # Independent-reader-friendly manifest and exact original/final state deltas.
    original_nonair = sum(v != 0 for v in old)
    unchanged_nonair = sum(v != 0 and v == blocks[i] for i,v in enumerate(old))
    final_counts = Counter(str(palette[v]['Name']) for v in blocks if v)
    report = {
        'original_file':Path(source).name,
        'original_sha256':hashlib.sha256(Path(source).read_bytes()).hexdigest(),
        'output_file':target.name,'output_sha256':hashlib.sha256(target.read_bytes()).hexdigest(),
        'output_bytes':target.stat().st_size,
        'dimensions_before':list(old_dims),'dimensions_after':[sx,sy,sz],
        'original_nonair':original_nonair,'final_nonair':sum(v != 0 for v in blocks),
        'original_nonair_same_state_and_position':unchanged_nonair,
        'author_preserved':str(md['Author']),
        'data_version_preserved':int(f['MinecraftDataVersion']),
        'floors':FLOORS,'simulation_distance_minimum_chunks':6,
        'afk_feet_local':[22.5,100,22.5],
        'remaining_source_water_count':sum(v != 0 and str(palette[v]['Name'])=='minecraft:water' and str(palette[v]['Properties']['level'])=='0' for v in blocks),
        'block_counts':dict(sorted(final_counts.items())),
        'edit_reasons':dict(Counter(e['reason'] for e in edits)),
        'live_bedrock_validation':False,
    }
    (outdir/'VERIFICACION.json').write_text(json.dumps(report,indent=2)+'\n')
    (outdir/'CAMBIOS-COORDENADAS.json').write_text(json.dumps({'coordinates':'normalized local, origin (0,0,0), before/after palette indices','palette':[p.unpack() for p in palette],'edits':edits},indent=2)+'\n')
    print(json.dumps(report,indent=2))

if __name__ == '__main__':
    adapt(sys.argv[1],sys.argv[2])
