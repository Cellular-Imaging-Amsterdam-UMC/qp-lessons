# Install QuPath 0.7.0 on your Mac

You need a Mac with Apple silicon (M1 or later) and macOS 15 or later. To check, choose **Apple menu > About This Mac**. Java, Python, extensions, and models are included.

## Install with a desktop shortcut

1. In Finder, open **Downloads** and double-click `QuPath7-StandAlone-Mac-Installer.zip`.
2. Open the extracted **QuPath7-StandAlone-Mac-Installer** folder and double-click **Install.command**. When Terminal shows **Installatie klaar** (Installation complete), press **Return** to close the installer.
3. Double-click **QuPath Standalone** on your desktop. Keep the new Terminal window open while using QuPath.

The installer puts QuPath in the **Applications** folder in your home folder (`~/Applications/QuPath7-StandAlone-Mac`). If the desktop shortcut is missing, open that folder and double-click **Start QuPath.command**.

## Extract and start manually

1. Find `QuPath7-StandAlone-Mac-0.7.0-arm64.tar.gz` in **Downloads** or in the extracted installer folder.
2. Double-click the archive. If a `.tar` file appears first, double-click it as well.
3. Open the extracted **QuPath7-StandAlone-Mac** folder. You can move the whole folder to **Documents** if you prefer. Keep its contents together.
4. Double-click **Start QuPath.command** and wait for the QuPath window to appear. Keep the Terminal window open while using QuPath.

Use **Start QuPath.command** each time so QuPath can find the included extensions and models.

## If macOS blocks the file

If macOS says it cannot verify the developer when you open **Install.command** or QuPath, follow these steps only if you got the archive from a source you trust.

1. After trying to open the file, go to **System Settings > Privacy & Security**.
2. Scroll down to **Security** and click **Open Anyway** next to the message for that file. Confirm by clicking **Open**.

[Safely open apps on your Mac (Apple Support)](https://support.apple.com/en-us/102445)
