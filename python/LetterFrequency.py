import sys
import time
import os
import unicodedata
from collections import Counter

def filter_characters(input_str, normalize):
    # Initialize an empty list to store filtered characters
    filtered_chars = []

    # Normalize the string if requested
    if normalize:
        input_str = unicodedata.normalize('NFD', input_str)

    # Iterate through each character in the input string
    filtered_chars = [char for char in input_str if ord(char) <= 0x024F and char.isalpha() and char not in 'ºª']

    # Join the filtered characters into a single string
    value = ''.join(filtered_chars)

    return value.lower()

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
            f"{letter}: {count / total_letters:.20f}\n"
            for letter, count in sorted(letter_counts.items())
        ]

        # Determine output file name based on combined input filenames
        output_file_name = generate_output_file_name(file_paths)

        # Specify the output directory relative to the current working directory
        output_directory = os.path.join(os.getcwd(), 'output_py')

        # Ensure the output directory exists, create if not
        if not os.path.exists(output_directory):
            os.makedirs(output_directory)

        # Construct the full output file path
        output_file_path = os.path.join(output_directory, output_file_name)

        # End the timer
        end_time = time.time()

        # Calculate the execution time
        execution_time = end_time - start_time

        # Save the execution time to the output file
        with open(output_file_path, 'w', encoding='utf-8') as output_file:
            output_file.writelines(output_content)
            output_file.write(f"\nExecution time: {execution_time} seconds\n")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    print("\n")
    print("Letter Frequency Analyzer")
    print("-------------------------")
    if len(sys.argv) < 3:
        print("This script calculates the frequency of each letter in a given text file.")
        print("The results are saved in a text file in the 'output_py' directory.")
        print("The output file name is generated based on the input file names.")
        print("The 'normalize' flag determines whether to merge accented characters.")
        print("The 'normalize' flag should be 'True' or 'False'.")
        print("\n")
        print("Usage: python LetterFrequency.py <file_path1> <file_path2> ... <normalize>")
        print("Example: python LetterFrequency.py path/to/textfile1.txt path/to/textfile2.txt True")
        print("\n")
    else:
        file_paths = sys.argv[1:-1]
        merge_accents = sys.argv[-1].lower() == 'true' or sys.argv[-1].lower() == 't' or sys.argv[-1].lower() == '1'
        letter_frequency(file_paths, merge_accents)
        print("Letter frequency analysis complete.")
        print("Output files saved in 'output_py' directory.")
        print("\n")
