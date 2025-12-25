# 🛡️ SainiON Hacks – JS Recon & Secret Scanner

![Python](https://img.shields.io/badge/Python-3.x-blue?style=for-the-badge&logo=python)
![Tkinter](https://img.shields.io/badge/GUI-Tkinter-green?style=for-the-badge)
![Security](https://img.shields.io/badge/Security-Recon-red?style=for-the-badge)
![YouTube](https://img.shields.io/badge/YouTube-SainiON%20Hacks-red?style=for-the-badge&logo=youtube)

A powerful **all-in-one GUI reconnaissance tool** for **Bug Bounty Hunters** and **Pentesters**.  
This tool automates JavaScript recon by pulling historical JS files from the **Wayback Machine**, scanning them for **hardcoded secrets**, and extracting **hidden endpoints**.

**Developed by:** SainiON Hacks  
📺 **YouTube:** https://www.youtube.com/@SainiONHacks  

---

## 📸 Screenshots

### 🔐 Secret Scanning (API Keys & Tokens)
![Secrets Tab](screenshots/secret_scan.png)  

### 🔎 Endpoint Discovery (Recon)
![Endpoints Tab](screenshots/endpoint_scan.png)  

---

## ✨ Features

- Wayback Machine (CDX API) integration  
- Smart URL filtering using URO  
- 900+ regex-based secret patterns  
- Shannon Entropy false-positive reduction  
- Endpoint extraction & auto-resolution  
- Tkinter-based professional GUI  
- HTML report export  

---

## 🚀 Installation

### Clone
```bash
git clone https://github.com/SainiONHacks/Secret_Hunter.git
cd Secret_Hunter
```

### Install
```bash
pip install -r requirements.txt
```

### Run
```bash
python main.py
```

---

## 🧪 Usage

1. Enter target domain  
2. Enable URO  
3. Click Start Recon  
4. Analyze Secrets & Endpoints  
5. Export HTML report  

---

## 📝 Configuration

Secrets are defined in `custom_secrets.json`

```json
[
  {
    "name": "Google API Key",
    "pattern": "AIza[0-9A-Za-z\-_]{35}",
    "severity": "medium"
  }
]
```

---

## ⚠️ Disclaimer

Educational and authorized testing only.  
You are responsible for legal usage.

---

## ❤️ Support

YouTube: https://www.youtube.com/@SainiONHacks

<script 
  src="https://cdnjs.buymeacoffee.com/1.0.0/button.prod.min.js"
  data-name="bmc-button"
  data-slug="nitinsainir"
  data-color="#FFDD00"
  data-font="Cookie"
  data-text="Buy me a coffee">
</script>
