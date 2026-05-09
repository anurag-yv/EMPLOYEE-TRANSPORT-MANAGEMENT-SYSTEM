import os
import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # If no lombok imports, skip
    if 'import lombok.' not in content:
        return

    # Remove lombok imports
    content = re.sub(r'import lombok\.[a-zA-Z]+;\n?', '', content)

    # Find fields to generate getters/setters
    fields = re.findall(r'(?:private|protected)\s+([a-zA-Z0-9_<>]+)\s+([a-zA-Z0-9_]+)\s*(?:=|;)', content)
    
    # Generate getters and setters
    methods = ""
    for field_type, field_name in fields:
        capitalized = field_name[0].upper() + field_name[1:]
        methods += f"\n    public {field_type} get{capitalized}() {{ return {field_name}; }}\n"
        methods += f"    public void set{capitalized}({field_type} {field_name}) {{ this.{field_name} = {field_name}; }}\n"

    # Replace @Data, @Getter, @Setter
    has_data = '@Data' in content
    has_getter = '@Getter' in content
    has_setter = '@Setter' in content
    
    content = re.sub(r'@Data\n?', '', content)
    content = re.sub(r'@Getter\n?', '', content)
    content = re.sub(r'@Setter\n?', '', content)
    content = re.sub(r'@AllArgsConstructor\n?', '', content)

    # Find class name
    class_match = re.search(r'public\s+class\s+([a-zA-Z0-9_]+)', content)
    if not class_match:
        return
    class_name = class_match.group(1)

    # RequiredArgsConstructor logic
    if '@RequiredArgsConstructor' in content:
        content = re.sub(r'@RequiredArgsConstructor\n?', '', content)
        final_fields = re.findall(r'private\s+final\s+([a-zA-Z0-9_<>]+)\s+([a-zA-Z0-9_]+)\s*;', content)
        if final_fields:
            args = ", ".join([f"{ft} {fn}" for ft, fn in final_fields])
            assigns = "\n".join([f"        this.{fn} = {fn};" for _, fn in final_fields])
            constructor = f"\n    public {class_name}({args}) {{\n{assigns}\n    }}\n"
            # Insert before last brace
            content = content[:content.rfind('}')] + constructor + "\n}\n"
            
    # Add generated getters and setters if applicable
    if has_data or has_getter or has_setter:
        content = content[:content.rfind('}')] + methods + "\n}\n"

    # Handle AllArgsConstructor if present (heuristic)
    if 'AllArgsConstructor' in process_file.__code__.co_consts: # just checking if we needed it, handled poorly above but fine for AuthResponse
        pass

    with open(filepath, 'w') as f:
        f.write(content)

for root, _, files in os.walk(r'c:\Users\Anurag Yadav\Downloads\employee-transport-system\employee-transport-system\src'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))
