import os
import shutil
import subprocess
import re

BOOKS_DIR = "books"
BOOKS_TEMP_DIR = "books_temp"
SCRIPT_PATH = "./bench.sh"


def reset_dir(directory):
    if os.path.exists(directory):
        shutil.rmtree(directory)
    os.makedirs(directory)
    print(f"Directory {directory} reset")


def copy_files_until_size(src_dir, dest_dir, max_size_mb):
    max_size = max_size_mb * 1024 * 1024
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)

    total_size = sum(
        os.path.getsize(os.path.join(dest_dir, f)) for f in os.listdir(dest_dir)
    )

    for filename in os.listdir(src_dir):
        src_file = os.path.join(src_dir, filename)
        dest_file = os.path.join(dest_dir, filename)

        if os.path.isfile(src_file):
            file_size = os.path.getsize(src_file)
            if total_size + file_size <= max_size:
                shutil.copy2(src_file, dest_file)
                total_size += file_size
            else:
                print(f"Reached the maximum size limit of {max_size / 1024 / 1024} MB")
                break


def parse_times(output: str):
    time_pattern = r"--TIME(\d):(\d+) ms--"

    matches = re.findall(time_pattern, output)

    if matches and len(matches) == 3:
        times = tuple(int(match[1]) for match in matches)
        return (times[0], times[1], times[2])
    else:
        return None


def resize_cluster(cluster: int):
    print(f"Resizing cluster to {cluster} nodes...")
    result = subprocess.run(
        ["./resize-cluster.sh", str(cluster)], stdout=subprocess.PIPE, text=True
    )
    print(f"Finished resizing cluster to {cluster} nodes!")


def run_benchmark(
    cluster: int, data_size: int, mapps: int, reduces: int
) -> (int, int, int):
    print(
        f"Running benchmark with size {data_size} MB, mappers {mapps} and reducers {reduces}..."
    )
    resize_cluster(cluster)
    reset_dir(BOOKS_TEMP_DIR)
    copy_files_until_size(BOOKS_DIR, BOOKS_TEMP_DIR, data_size)
    result = subprocess.run(
        [SCRIPT_PATH, str(mapps), str(reduces)], stdout=subprocess.PIPE, text=True
    )
    print(
        f"Finished benchmark with size {data_size} MB, mappers {mapps} and reducers {reduces}!"
    )
    output = result.stdout
    times = parse_times(output)
    if times is None:
        print("Error parsing output")
        return
    else:
        print(f"Times: {times}")
        print(f"Total: {sum(times)/1000} s")
    with open(f"logs/{cluster}_{data_size}_{mapps}_{reduces}_log.txt", "w") as file:
        file.write(output)
    return times


def append_to_bench(
    name: str,
    columns: str,
    values,
):
    file_path = f"benchs/{name}_bench.txt"
    if not os.path.exists(file_path):
        with open(file_path, "w") as file:
            file.write(columns + "\n")
    with open(file_path, "a") as file:
        file.write(",\t".join(str(value) for value in values) + "\n")


def check_cluster_size():
    for i in [6, 4, 2]:
        for size in [1, 10, 45, 90]:
            result = run_benchmark(i, size, 0, 0)
            append_to_bench(
                "check_cluster",
                "slaves,\t mb,\t mstf,\t msdf,\t mstfidf",
                (i - 1, size, *result),
            )


def check_mapp_red():
    for m in [5, 25, 250]:
        for r in [1, 2, 8, 16]:
            result = run_benchmark(6, 10, m, r)
            append_to_bench(
                "check_map_red_n",
                "mapps,\t reduces,\t mstf,\t msdf,\t mstfidf",
                (m, r, *result),
            )


def check_data_size():
    for size in range(10, 91, 10):
        result = run_benchmark(6, size, 0, 0)
        append_to_bench(
            "check_data_size", "mb,\t mstf,\t msdf,\t mstfidf", (size, *result)
        )


def main():
    check_cluster_size()
    check_mapp_red()
    check_data_size()


if __name__ == "__main__":
    main()
