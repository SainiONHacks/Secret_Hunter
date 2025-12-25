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
