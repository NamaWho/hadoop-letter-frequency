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
    return f"Letter_Count_{combined_name}.txt"

def letter_count(file_paths, normalize):
    # Start the timer
    start_time = time.time()

    try:
        # Merge contents of all files
        merged_content = merge_files(file_paths)

        # Normalize content based on normalize flag
        content = filter_characters(merged_content, normalize)

        # Count the total number of letters and the frequency of each letter
        letter_counts = Counter(content)
        total_letters = sum(letter_counts.values())

        # Prepare the output content
        output_content = [
            f"Total letters: {total_letters}\n",
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