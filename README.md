# 🛡️ SainiON Hacks - JS Recon & Secret Scanner

![Python](https://img.shields.io/badge/Python-3.x-blue?style=for-the-badge&logo=python)
![Tkinter](https://img.shields.io/badge/GUI-Tkinter-green?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-Recon-red?style=for-the-badge)
![YouTube](https://img.shields.io/badge/YouTube-SainiON%20Hacks-red?style=for-the-badge&logo=youtube&link=https://www.youtube.com/@SainiONHacks)

A powerful, all-in-one GUI tool for Bug Bounty Hunters and Pentesters. This tool automates the process of fetching JavaScript files from the Wayback Machine, scanning them for hardcoded secrets (API keys, tokens), and discovering hidden endpoints (API routes, absolute URLs).

**Developed by:** SainiON Hacks  
**YouTube:** [Subscribe for more Hacking Tools](https://www.youtube.com/@SainiONHacks)

---

## 📸 Screenshots

### 1. Secret Scanning (API Keys & Tokens)
![Secrets Tab](screenshots/secret_scan.png)
*Detects high-entropy strings and uses Regex signatures to find credentials.*

### 2. Endpoint Discovery (Recon)
![Endpoints Tab](screenshots/endpoint_scan.png)
*Automatically converts relative paths (e.g., `../api/v1/user`) into absolute URLs.*

---

## ✨ Features

* **🔍 Wayback Machine Integration:** Auto-fetch all historical JS files for a target domain using the CDX API.
* **🧹 Smart Filtering (URO):** Built-in "Url Retaining Option" logic to remove duplicate/junk URLs (e.g., stripping `?v=1.2` parameters).
* **🔓 Secret Scanner:**
    * Uses a custom `custom_secrets.json` database with 900+ Regex patterns.
    * **Entropy Check:** Calculates Shannon Entropy to reduce false positives (ignores "dummy" strings).
    * **False Positive Filters:** Automatically skips `jquery`, `bootstrap`, and common variable names.
* **🔗 Endpoint Extractor:**
    * Extracts paths like `/api/user`, `http://...`, and `../admin`.
    * **Auto-Resolve:** Converts relative paths found inside JS into clickable absolute URLs based on the source file location.
* **🖥️ Professional GUI:**
    * Tabbed interface (Secrets vs. Endpoints).
    * Right-click context menu (Copy Secret, Open URL).
    * Real-time progress bar and stats.
* **💾 Reporting:** Export full results to a color-coded HTML report.

---

## 🚀 Installation

### Prerequisites
* Python 3.x
* Pip

### 1. Clone the Repository
```bash
git clone [https://github.com/YourUsername/JS-Recon-Tool.git](https://github.com/YourUsername/JS-Recon-Tool.git)
cd JS-Recon-Tool

Here is the updated README.md file. I have removed the "Building an EXE" section and ensured all the details are self-contained and professional for your GitHub repository.
📄 README.md

Create a file named README.md and paste this exact code:
Markdown

# 🛡️ SainiON Hacks - JS Recon & Secret Scanner

![Python](https://img.shields.io/badge/Python-3.x-blue?style=for-the-badge&logo=python)
![Tkinter](https://img.shields.io/badge/GUI-Tkinter-green?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-Recon-red?style=for-the-badge)
![YouTube](https://img.shields.io/badge/YouTube-SainiON%20Hacks-red?style=for-the-badge&logo=youtube&link=https://www.youtube.com/@SainiONHacks)

A powerful, all-in-one GUI tool for Bug Bounty Hunters and Pentesters. This tool automates the process of fetching JavaScript files from the Wayback Machine, scanning them for hardcoded secrets (API keys, tokens), and discovering hidden endpoints (API routes, absolute URLs).

**Developed by:** SainiON Hacks  
**YouTube:** [Subscribe for more Hacking Tools](https://www.youtube.com/@SainiONHacks)

---

## 📸 Screenshots

### 1. Secret Scanning (API Keys & Tokens)
![Secrets Tab](screenshots/secret_scan.png)
*Detects high-entropy strings and uses Regex signatures to find credentials.*

### 2. Endpoint Discovery (Recon)
![Endpoints Tab](screenshots/endpoint_scan.png)
*Automatically converts relative paths (e.g., `../api/v1/user`) into absolute URLs.*

---

## ✨ Key Features

### 1. 🔍 Automated Recon
* **Wayback Machine Integration:** Automatically fetches all historical JavaScript files for a target domain using the CDX API.
* **Smart Filtering (URO):** Built-in "Url Retaining Option" logic that removes duplicate or junk URLs (e.g., stripping `?v=1.2` or `?timestamp=` parameters) to speed up scanning.

### 2. 🔓 Advanced Secret Scanner
* **Massive Database:** Uses a custom `custom_secrets.json` file loaded with **900+ Regex signatures** for finding API keys, AWS credentials, Slack tokens, and more.
* **Entropy Analysis:** Calculates Shannon Entropy to distinguish between real secrets (high randomness) and fake dummy data (low randomness).
* **False Positive Reduction:** Automatically skips junk files (like `jquery.min.js`, `bootstrap.js`) and ignores common false positive keywords (like `example`, `null`, `test`).

### 3. 🔗 Endpoint Extractor
* **Discovery:** Extracts hidden API routes, relative paths, and full URLs from inside the JavaScript code.
* **Auto-Resolution:** Smartly converts relative paths (like `../admin/login`) into full clickable URLs based on the source file's location.

### 4. 🖥️ Professional GUI
* **Dual Tabs:** Separate views for "Secrets" and "Endpoints".
* **Context Menu:** Right-click on any result to **Copy Secret**, **Copy URL**, or **Open in Browser**.
* **Live Stats:** Real-time progress bar showing scanned files vs. discovered items.
* **Branding:** Custom SainiON Hacks branding and direct YouTube access.

### 5. 💾 Reporting
* **HTML Export:** Generates a clean, color-coded HTML report of all findings for documentation or client reporting.

---

## 🚀 Installation & Usage

### Prerequisites
* Python 3.x
* Pip

### 1. Clone the Repository
```bash
git clone [https://github.com/YourUsername/JS-Recon-Tool.git](https://github.com/YourUsername/JS-Recon-Tool.git)
cd JS-Recon-Tool

### 2. Install Dependencies
Bash

pip install -r requirements.txt

### 3. Run the Tool
Bash

python scanner_final.py

### 4. Start Scanning

    Enter Target: Type the domain name (e.g., tesla.com).

    URO Checkbox: Keep "Use URO" checked to enable smart filtering.

    Start Recon: Click 🚀 Start Recon.

 ##   Analyze Results:

        Secrets Tab: Look for Red (Critical) items.

        Endpoints Tab: Check for interesting API paths.

    Export: Click 💾 Export Report to save your findings.

##📝 Configuration

The tool uses a JSON file to define what secrets to look for. You can edit custom_secrets.json to add your own patterns.

File Structure (custom_secrets.json):
JSON

[
  {
    "name": "Google API Key",
    "pattern": "AIza[0-9A-Za-z\\-_]{35}",
    "severity": "medium"
  },
  {
    "name": "Slack Token",
    "pattern": "(xox[p|b|o|a]-[0-9]{12}-[0-9]{12}-[0-9]{12}-[a-z0-9]{32})",
    "severity": "high"
  }
]

##⚠️ Disclaimer

This tool is designed for educational purposes and authorized security testing only. The developer (SainiON Hacks) is not responsible for any misuse.

By using this software, you agree to:

    Only scan domains you own or have explicit permission to test.

    Comply with all local and international cyber laws.

    Not use this tool for malicious purposes.

## ❤️ Support

If you find this tool useful, please Subscribe to SainiON Hacks on YouTube for more ethical hacking content!

<script type="text/javascript" src="https://cdnjs.buymeacoffee.com/1.0.0/button.prod.min.js" data-name="bmc-button" data-slug="nitinsainir" data-color="#FFDD00" data-emoji=""  data-font="Cookie" data-text="Buy me a coffee" data-outline-color="#000000" data-font-color="#000000" data-coffee-color="#ffffff" ></script>
