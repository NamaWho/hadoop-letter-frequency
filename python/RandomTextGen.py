import argparse
import random
import string

def generate_random_file(file_size_mb, include_all_ascii):
    file_size = int(file_size_mb * 1024 * 1024)  # convert MB to bytes
    
    if include_all_ascii:
        ascii_chars = string.ascii_letters + string.digits + string.punctuation + ' '
        file_name = 'random_letters_all.txt'
    else:
        ascii_chars = string.ascii_letters
        file_name = 'random_letters.txt'
    
    

    with open(file_name, 'w') as file:
        bytes_written = 0
        while bytes_written < file_size:
            # Generate a random string of variable length (limited to not exceed remaining file size)
            random_string = ''.join(random.choice(ascii_chars) for _ in range(min(1024, file_size - bytes_written)))
            file.write(random_string)
            bytes_written += len(random_string)

    print(f"File '{file_name}' successfully created with size approximately {file_size_mb} MB.")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate a file of specified size containing random characters.')
    parser.add_argument('file_size_mb', type=float, help='File size in MB (e.g., 600 for 600 MB)')
    parser.add_argument('--all_ascii', action='store_true', help='Include all ASCII characters (letters, numbers, and special characters)')

    args = parser.parse_args()

    generate_random_file(args.file_size_mb, args.all_ascii)
