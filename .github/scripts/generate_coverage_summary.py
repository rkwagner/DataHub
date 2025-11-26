import os
import glob
import xml.etree.ElementTree as ET
import sys

def parse_jacoco_xml(xml_path):
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()
        
        counters = {
            'INSTRUCTION': {'missed': 0, 'covered': 0},
            'BRANCH': {'missed': 0, 'covered': 0},
            'LINE': {'missed': 0, 'covered': 0},
            'COMPLEXITY': {'missed': 0, 'covered': 0},
            'METHOD': {'missed': 0, 'covered': 0},
            'CLASS': {'missed': 0, 'covered': 0}
        }
        
        for counter in root.findall('counter'):
            ctype = counter.get('type')
            if ctype in counters:
                counters[ctype]['missed'] = int(counter.get('missed'))
                counters[ctype]['covered'] = int(counter.get('covered'))
                
        return counters
    except Exception as e:
        print(f"Error parsing {xml_path}: {e}")
        return None

def calculate_percentage(covered, missed):
    total = covered + missed
    if total == 0:
        return 0.0
    return (covered / total) * 100

def get_status_emoji(percentage):
    if percentage >= 80:
        return "🟢"
    elif percentage >= 60:
        return "🟡"
    else:
        return "🔴"

def main():
    search_path = sys.argv[1] if len(sys.argv) > 1 else "."
    xml_files = glob.glob(f"{search_path}/**/jacocoTestReport.xml", recursive=True)
    
    # Also look for the root report
    root_report = glob.glob(f"{search_path}/**/jacocoRootReport.xml", recursive=True)
    
    modules_data = []
    
    for xml_file in xml_files:
        # Extract module name from path (e.g., /core/build/reports/...)
        parts = xml_file.replace("\\", "/").split("/")
        module_name = "Unknown"
        if "build" in parts:
            idx = parts.index("build")
            if idx > 0:
                module_name = parts[idx-1]
        
        # Skip if it's the root report in the list (though glob shouldn't find it if named differently)
        if "jacocoRootReport" in xml_file:
            continue
            
        counters = parse_jacoco_xml(xml_file)
        if counters:
            modules_data.append((module_name, counters))
            
    # Sort by module name
    modules_data.sort(key=lambda x: x[0])
    
    # Generate Markdown
    md_lines = []
    md_lines.append("## 📊 Code Coverage Report")
    md_lines.append("")
    md_lines.append("| Module | Instruction Coverage | Branch Coverage | Complexity |")
    md_lines.append("| :--- | :---: | :---: | :---: |")
    
    total_inst_missed = 0
    total_inst_covered = 0
    
    for module, counters in modules_data:
        inst_missed = counters['INSTRUCTION']['missed']
        inst_covered = counters['INSTRUCTION']['covered']
        branch_missed = counters['BRANCH']['missed']
        branch_covered = counters['BRANCH']['covered']
        comp_missed = counters['COMPLEXITY']['missed']
        comp_covered = counters['COMPLEXITY']['covered']
        
        total_inst_missed += inst_missed
        total_inst_covered += inst_covered
        
        inst_pct = calculate_percentage(inst_covered, inst_missed)
        branch_pct = calculate_percentage(branch_covered, branch_missed)
        comp_pct = calculate_percentage(comp_covered, comp_missed)
        
        md_lines.append(f"| **{module}** | {inst_pct:.2f}% {get_status_emoji(inst_pct)} | {branch_pct:.2f}% | {comp_pct:.2f}% |")
        
    # Overall row
    if root_report:
        root_counters = parse_jacoco_xml(root_report[0])
        if root_counters:
            inst_missed = root_counters['INSTRUCTION']['missed']
            inst_covered = root_counters['INSTRUCTION']['covered']
            branch_missed = root_counters['BRANCH']['missed']
            branch_covered = root_counters['BRANCH']['covered']
            comp_missed = root_counters['COMPLEXITY']['missed']
            comp_covered = root_counters['COMPLEXITY']['covered']
            
            inst_pct = calculate_percentage(inst_covered, inst_missed)
            branch_pct = calculate_percentage(branch_covered, branch_missed)
            comp_pct = calculate_percentage(comp_covered, comp_missed)
            
            md_lines.append(f"| **TOTAL** | **{inst_pct:.2f}%** {get_status_emoji(inst_pct)} | **{branch_pct:.2f}%** | **{comp_pct:.2f}%** |")
    
    print("\n".join(md_lines))
    
    # Write to file
    with open("coverage-summary.md", "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))

if __name__ == "__main__":
    main()
