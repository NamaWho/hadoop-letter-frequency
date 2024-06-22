import os

def iterate_and_process(root_folder, old_name, new_name):
    for root, dirs, files in os.walk(root_folder):
        for file in files:
            if old_name in file:
                old_file_path = os.path.join(root, file)
                new_file = file.replace(old_name, new_name)
                new_file_path = os.path.join(root, new_file)
                os.rename(old_file_path, new_file_path)
                print(f"Renamed file: {file} to {new_file}")
        
        for dir in dirs:
            if old_name in dir:
                old_dir_path = os.path.join(root, dir)
                new_dir = dir.replace(old_name, new_name)
                new_dir_path = os.path.join(root, new_dir)
                os.rename(old_dir_path, new_dir_path)
                print(f"Renamed folder: {dir} to {new_dir}")

                # Now recurse into the renamed subdirectory
                iterate_and_process(new_dir_path, old_name, new_name)

def get_dir_path(folder_name):
    current_dir = os.getcwd()
    parent_dir = os.path.join(current_dir, os.pardir)
    return os.path.join(parent_dir, folder_name)

def main():
    print("Renaming all folders and files")
    folder = input("Enter the folder name to rename: ")
    root_folder = get_dir_path(folder)
    print(f"Root folder: {root_folder}")
    old_name = input("Enter the old name to replace: ")
    new_name = input("Enter the new name: ")
    old_name = old_name.strip()
    new_name = new_name.strip()
    iterate_and_process(root_folder, old_name, new_name)
    
if __name__ == "__main__":
    main()
