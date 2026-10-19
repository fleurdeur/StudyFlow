#ifndef STUDYFLOW_NATIVE_H
#define STUDYFLOW_NATIVE_H

#include <string>
#include <vector>
#include <cstdint>
#include "slab_allocator.h"
#include "vector_search.h"

namespace studyflow {

struct FlashcardNative {
    std::string question;
    std::string answer;
    std::string topic;
    int difficulty; // 1: Easy, 2: Medium, 3: Hard
};

struct DocumentSummary {
    std::string title;
    std::string courseCode;
    std::vector<std::string> keyPoints;
    std::vector<FlashcardNative> cards;
    int estimatedReadMinutes;
};

struct CategorizationResult {
    std::string courseCode;
    std::string topic;
    std::string category;
    double confidence;
};

// Core high-performance parsing & extraction
DocumentSummary parseDocumentBuffer(const uint8_t* buffer, size_t size, const std::string& fileName);
DocumentSummary parseRawLectureText(const std::string& text, const std::string& sourceName);

// Fast categorization & topic linkage
CategorizationResult categorizeContent(const std::string& title, const std::string& content);

// Active recall & flashcard synthesis
std::vector<FlashcardNative> generateFlashcards(const std::string& text, int targetCount = 10);

// Spaced repetition mastery algorithm (SM-2 based)
double calculateMasteryScore(int totalCards, int masteredCards, int reviewStreak);

} // namespace studyflow

#endif // STUDYFLOW_NATIVE_H
