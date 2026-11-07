param(
    [string]$DocsDir = "$PSScriptRoot\markdown",
    [string]$OutFile = "$PSScriptRoot\static\search-index.json"
)

$docs = [System.Collections.Generic.List[PSObject]]::new()

# Scan markdown files
$files = Get-ChildItem -Path $DocsDir -Recurse -Filter "*.md"

foreach ($file in $files) {
    $relPath = $file.FullName.Substring($DocsDir.Length + 1).Replace('\', '/')
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    
    # Determine default category
    $category = "Documentation"
    if ($relPath -like "reference/blocks/*") {
        $category = "Blocks"
    } elseif ($relPath -like "reference/components/*") {
        $category = "Components"
    } elseif ($relPath -like "*tutorial*") {
        $category = "Tutorials"
    } elseif ($relPath -like "*faq*") {
        $category = "FAQs"
    }

    # Extract component sections if inside reference/components
    if ($relPath -like "reference/components/*.md") {
        $htmlBase = "/reference/components/" + [System.IO.Path]::GetFileNameWithoutExtension($file.Name) + ".html"
        $lines = $content -split "`r?`n"
        $currentSection = ""
        $currentAnchor = ""
        $currentBody = [System.Text.StringBuilder]::new()

        foreach ($line in $lines) {
            if ($line -match '^##\s+([A-Za-z0-9_]+)\s*(\{#([A-Za-z0-9_\-\.]+)\})?') {
                if ($currentSection -ne "" -and $currentSection -ne "Table of Contents") {
                    $url = $htmlBase + $(if ($currentAnchor) { "#$currentAnchor" } else { "" })
                    $docs.Add([PSCustomObject]@{
                        id = "$relPath#$currentSection"
                        title = $currentSection
                        category = "Documentation"
                        url = $url
                        content = $currentBody.ToString()
                    })
                }
                $currentSection = $matches[1]
                $currentAnchor = if ($matches[3]) { $matches[3] } else { $currentSection }
                $currentBody.Clear() | Out-Null
            } else {
                [void]$currentBody.AppendLine($line)
            }
        }
        if ($currentSection -ne "") {
            $url = $htmlBase + $(if ($currentAnchor) { "#$currentAnchor" } else { "" })
            $docs.Add([PSCustomObject]@{
                id = "$relPath#$currentSection"
                title = $currentSection
                category = "Documentation"
                url = $url
                content = $currentBody.ToString()
            })
        }
    } else {
        $title = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
        if ($content -match '(?m)^title:\s*(.+)$') {
            $title = $matches[1].Trim()
        } elseif ($content -match '(?m)^#\s+(.+)$') {
            $title = $matches[1].Trim()
        }
        $urlPath = "/" + $relPath.Substring(0, $relPath.Length - 3) + ".html"
        $docs.Add([PSCustomObject]@{
            id = $relPath
            title = $title
            category = $category
            url = $urlPath
            content = $content
        })
    }
}

# Add tutorials and example projects resources
$tutorialsAndExamples = @(
    @{
        id = "tut-camera";
        title = "Using the Camera in Your App";
        category = "Tutorials";
        url = "http://appinventor.mit.edu/explore/ai2/tutorials";
        content = "Step-by-step instructions for adding camera functionality, taking pictures, and saving photos."
    },
    @{
        id = "faq-camera-perms";
        title = "Camera Permission and Storage Access Issues";
        category = "FAQs";
        url = "https://community.appinventor.mit.edu";
        content = "Common camera issues, runtime permissions on Android, error messages, and troubleshooting camera access."
    },
    @{
        id = "example-photo-capture";
        title = "Photo Capture & Image Filter Sample Project";
        category = "Examples";
        url = "http://appinventor.mit.edu/explore/resources";
        content = "Sample application project demonstrating Camera, Canvas, ImagePicker, and saving photos to Gallery."
    },
    @{
        id = "tut-bluetooth";
        title = "Bluetooth Client and Server Communication";
        category = "Tutorials";
        url = "http://appinventor.mit.edu/explore/ai2/tutorials";
        content = "Tutorial on connecting via BluetoothClient, scanning devices, and sending data to Arduino."
    },
    @{
        id = "example-drawing-app";
        title = "PaintPot: Drawing and Color Palette App";
        category = "Examples";
        url = "http://appinventor.mit.edu/explore/ai2/paintpot-all";
        content = "Step-by-step example project creating a painting and drawing app using Canvas and Camera."
    }
)

foreach ($item in $tutorialsAndExamples) {
    $docs.Add([PSCustomObject]$item)
}

$parentDir = [System.IO.Path]::GetDirectoryName($OutFile)
if (-not (Test-Path $parentDir)) {
    New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
}

$json = ConvertTo-Json -InputObject @{ docs = $docs } -Depth 4
[System.IO.File]::WriteAllText($OutFile, $json, [System.Text.Encoding]::UTF8)
Write-Output "Successfully generated search index with $($docs.Count) entries to $OutFile"
