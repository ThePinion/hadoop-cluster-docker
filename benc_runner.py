import os
import shutil

BOOKS_DIR = "books"
BOOKS_TEMP_DIR = "books_temp"

def reset_dir(directory):
    if os.path.exists(directory):
        shutil.rmtree(directory)
    os.makedirs(directory)
    print(f"Directory {directory} reset")

def copy_files_until_size(src_dir, dest_dir, max_size_mb):
    max_size = max_size_mb * 1024 * 1024
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
    
    total_size = sum(os.path.getsize(os.path.join(dest_dir, f)) for f in os.listdir(dest_dir))
    
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

def main():
    reset_dir(BOOKS_TEMP_DIR)
    copy_files_until_size(BOOKS_DIR, BOOKS_TEMP_DIR, 1)

if __name__ == "__main__":
    main()