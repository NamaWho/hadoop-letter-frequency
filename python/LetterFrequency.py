import sys
import time
import os
import unicodedata
import re
from collections import Counter


def filter_characters(input_str, normalize):
    # Initialize an empty list to store filtered characters
    filtered_chars = []
    
    # Iterate through each character in the input string
    filtered_chars = [char for char in input_str if 0x0000 <= ord(char) <= 0x024F]
    
    # Join the filtered characters into a single string
    value = ''.join(filtered_chars)
    
    # Apply regex to remove unwanted characters and convert to lowercase
    value = re.sub(r'[\W\d_\sªº]', '', value).lower()
    
    # Normalize the string if requested
    if normalize:
        value = unicodedata.normalize('NFD', value)
        # Remove combining diacritical marks
        value = re.sub(r'[\u0300-\u036f]', '', value)
    
    return value

def merge_files(file_paths):
    merged_content = ""
    for file_path in file_paths:
        try:
            with open(file_path, 'r', encoding='utf-8') as file:
                content = file.read().lower()
                merged_content += content
        except FileNotFoundError:
            print(f"Error: File '{file_path}' not found.")
        except Exception as e:
            print(f"An error occurred while reading '{file_path}': {e}")
    return merged_content

def generate_output_file_name(file_paths):
    # Extract filenames without extension
    base_names = [os.path.splitext(os.path.basename(fp))[0] for fp in file_paths]
    # Concatenate filenames
    combined_name = "_".join(base_names)
    # Return the formatted output file name
    return f"Letter_Frequency_{combined_name}.txt"

def letter_frequency(file_paths, merge_accents):
    # Start the timer
    start_time = time.time()

    try:
        # Merge contents of all files
        merged_content = merge_files(file_paths)

        # Normalize content based on merge_accents flag
        content = filter_characters(merged_content, merge_accents)

        # Count the total number of letters and the frequency of each letter
        letter_counts = Counter(content)
        total_letters = sum(letter_counts.values())

        # Prepare the output content
        output_content = [
            f"Total letters: {total_letters}\n\n",
        ]
        output_content += [
            f"{letter}: {count / total_letters:.16f}\n"
            for letter, count in sorted(letter_counts.items())
        ]

        # Determine output file name based on combined input filenames
        output_file_name = generate_output_file_name(file_paths)
        output_file_path = os.path.join(os.path.dirname(file_paths[0]), output_file_name)

        # Write the output to the file
        with open(output_file_path, 'w', encoding='utf-8') as output_file:
            output_file.writelines(output_content)

        # End the timer
        end_time = time.time()

        # Calculate the execution time
        execution_time = end_time - start_time

        # Save the execution time to the output file
        with open(output_file_path, 'a', encoding='utf-8') as output_file:
            output_file.write(f"\nExecution time: {execution_time} seconds\n")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python script.py <file_path1> <file_path2> ... <merge_accents>")
        print("Example: python script.py path/to/textfile1.txt path/to/textfile2.txt True")
    else:
        file_paths = sys.argv[1:-1]
        merge_accents = sys.argv[-1].lower() == 'true'
        letter_frequency(file_paths, merge_accents)