import json
import csv
import os

ARQUIVO_CSV = 'data.csv'

with open(ARQUIVO_CSV, 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f, delimiter=';')
    writer.writerow([
        'Atom', 'Version', 'Execution', 'ns/op', 'op/s', 'ins/op', 
        'bra/op', 'IPC', 'branch miss (%)', 'Dr', 'Dw', 'D1mr', 'D1mw'
    ])

    linhas_salvas = 0

    for i in range(1, 31):
        filename = f"run_{i}.json"
        if not os.path.exists(filename):
            continue

        with open(filename, 'r', encoding='utf-8') as json_file:
            data = json.load(json_file)

        for benchmark in data:
            full_name = benchmark.get('benchmark', 'Unknown')
            if full_name.endswith('NAC'):
                atom, version = full_name.split('.')[-1][:-3], 'NAC'
            elif full_name.endswith('AC'):
                atom, version = full_name.split('.')[-1][:-2], 'AC'
            else:
                atom, version = full_name, 'Unknown'

            ns = benchmark.get('primaryMetric', {}).get('score', 0)

            def get_hw_score(keyword):
                sec_metrics = benchmark.get('secondaryMetrics', {})
                for key, val in sec_metrics.items():
                    if keyword in key:
                        return val.get('score', 0)
                return 0

            ins   = get_hw_score('instructions')
            cyc   = get_hw_score('cycles')
            bra   = get_hw_score('branches')
            bmiss = get_hw_score('branch-misses')
            dr    = get_hw_score('L1-dcache-loads')
            dw    = get_hw_score('L1-dcache-stores')
            d1mr  = get_hw_score('L1-dcache-load-misses')
            d1mw  = get_hw_score('L1-dcache-store-misses')

            # Filtro contra falha da PMU
            if ins == 0:
                continue

            ops       = (1_000_000_000 / ns) if ns > 0 else 0
            ipc       = (ins / cyc) if cyc > 0 else 0
            bmiss_pct = (bmiss / bra * 100) if bra > 0 else 0

            def fmt(valor, casas=4):
                return f"{valor:.{casas}f}".replace('.', ',')

            writer.writerow([
                atom, version, i, 
                fmt(ns), fmt(ops, 2), fmt(ins), 
                fmt(bra), fmt(ipc), fmt(bmiss_pct), 
                fmt(dr), fmt(dw), fmt(d1mr), fmt(d1mw)
            ])
            linhas_salvas += 1

print(f"Extraction Finished! {linhas_salvas} runs recorded with success.")