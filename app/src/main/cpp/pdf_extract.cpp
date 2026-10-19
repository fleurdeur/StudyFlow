#include "native.h"
#include <sstream>
#include <algorithm>
#include <cctype>

namespace studyflow {

static std::string trim(const std::string& str) {
    size_t first = str.find_first_not_of(" \t\r\n");
    if (first == std::string::npos) return "";
    size_t last = str.find_last_not_of(" \t\r\n");
    return str.substr(first, (last - first + 1));
}

DocumentSummary parseDocumentBuffer(const uint8_t* buffer, size_t size, const std::string& fileName) {
    // High-performance direct buffer stream extraction
    std::string textContent;
    textContent.reserve(size);

    bool insideTextObject = false;
    for (size_t i = 0; i < size; ++i) {
        // Simple PDF text object detection: BT ... ET
        if (i + 2 < size && buffer[i] == 'B' && buffer[i+1] == 'T') {
            insideTextObject = true;
            i += 1;
            continue;
        } else if (i + 2 < size && buffer[i] == 'E' && buffer[i+1] == 'T') {
            insideTextObject = false;
            textContent += "\n";
            i += 1;
            continue;
        }

        if (insideTextObject) {
            // Check for PDF string literals (...)
            if (buffer[i] == '(') {
                size_t j = i + 1;
                while (j < size && buffer[j] != ')') {
                    if (buffer[j] == '\\' && j + 1 < size) j++;
                    textContent += static_cast<char>(buffer[j]);
                    j++;
                }
                textContent += " ";
                i = j;
            }
        } else {
            // General printable ASCII pass-through for plaintext or slide dumps
            char c = static_cast<char>(buffer[i]);
            if (std::isprint(static_cast<unsigned char>(c)) || c == '\n' || c == '\t') {
                textContent += c;
            }
        }
    }

    if (textContent.empty()) {
        textContent = "Document: " + fileName;
    }

    return parseRawLectureText(textContent, fileName);
}

DocumentSummary parseRawLectureText(const std::string& text, const std::string& sourceName) {
    DocumentSummary summary;
    summary.title = sourceName;
    summary.estimatedReadMinutes = std::max(1, static_cast<int>(text.length() / 800));

    // Smart course detection
    CategorizationResult cat = categorizeContent(sourceName, text);
    summary.courseCode = cat.courseCode;

    std::istringstream stream(text);
    std::string line;
    while (std::getline(stream, line)) {
        line = trim(line);
        if (line.empty()) continue;

        // Check if line is a bullet or high-yield summary statement
        if (line.rfind("- ", 0) == 0 || line.rfind("• ", 0) == 0 || line.rfind("* ", 0) == 0) {
            std::string cleaned = trim(line.substr(2));
            if (cleaned.length() > 10) {
                summary.keyPoints.push_back(cleaned);
            }
        } else if (line.find(":") != std::string::npos && line.length() > 15 && line.length() < 160) {
            // Key term or definition line
            summary.keyPoints.push_back(line);
        } else if (summary.keyPoints.size() < 12 && line.length() > 25 && line.length() < 200) {
            summary.keyPoints.push_back(line);
        }
    }

    // Generate flashcards from extracted content
    summary.cards = generateFlashcards(text, 10);
    return summary;
}

} // namespace studyflow
