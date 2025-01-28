#!/bin/bash

# Base directory to search
BASE_DIR="judo-ui-react-itest"

# Temporary file to store folder line counts
temp_file=$(mktemp)

# Initialize the temporary file
> "$temp_file"å

# Find all .ts and .tsx files in the specified folder structure
find "$BASE_DIR"/*/*/target/frontend-react/src -type f \( -name "*.ts" -o -name "*.tsx" \) | while read -r file; do
  # Extract the root folder name under judo-ui-react-itest
  root_folder=$(echo "$file" | awk -F'/' '{print $2}')

  # Count the number of lines in the file, ignoring lines that start with "//"
  line_count=$(grep -vE '^\s*//' "$file" | wc -l)

  # Append the root folder and line count to the temporary file
  echo "$root_folder $line_count" >> "$temp_file"
done

# Aggregate line counts per root folder and sort them
{
  echo "Summary of line counts per root folder:"
  sort "$temp_file" | awk '{count[$1] += $2} END {
    for (folder in count) printf "%s: %d lines\n", folder, count[folder]
  }' | sort
}

# Clean up
rm -f "$temp_file"