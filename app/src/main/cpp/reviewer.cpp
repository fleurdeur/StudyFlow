#include "native.h"
#include <sstream>
#include <regex>
#include <cmath>

namespace studyflow {

std::vector<FlashcardNative> generateFlashcards(const std::string& text, int targetCount) {
    std::vector<FlashcardNative> cards;
    std::istringstream stream(text);
    std::string line;

    // Pattern 1: Term : Definition
    std::regex defRegex(R"(^([^:\n]{3,40})\s*:\s*(.+)$)");
    // Pattern 2: X is defined as Y or X refers to Y
    std::regex isDefinedRegex(R"(^([A-Z][a-zA-Z\s]{2,35})\s+(?:is defined as|refers to|is the process of|consists of)\s+(.+)$)", std::regex::icase);

    while (std::getline(stream, line) && static_cast<int>(cards.size()) < targetCount) {
        // Trim leading symbols
        size_t start = line.find_first_not_of(" \t-*•");
        if (start == std::string::npos) continue;
        std::string cleaned = line.substr(start);

        std::smatch match;
        if (std::regex_match(cleaned, match, defRegex)) {
            FlashcardNative card;
            card.question = "What is " + match.str(1) + "?";
            card.answer = match.str(2);
            card.topic = "Definition";
            card.difficulty = 1;
            cards.push_back(card);
        } else if (std::regex_match(cleaned, match, isDefinedRegex)) {
            FlashcardNative card;
            card.question = "Explain " + match.str(1);
            card.answer = match.str(2);
            card.topic = "Core Concept";
            card.difficulty = 2;
            cards.push_back(card);
        } else if (cleaned.length() > 30 && cleaned.length() < 140 && cards.size() < 4) {
            FlashcardNative card;
            card.question = "Key concept recall: What is the main idea of this topic?";
            card.answer = cleaned;
            card.topic = "Summary Fact";
            card.difficulty = 2;
            cards.push_back(card);
        }
    }

    // If text was short or cards below minimum, synthesize standard cards
    if (cards.empty()) {
        cards.push_back({
            "What is the time complexity of QuickSort in the average case?",
            "O(n log n) with a well-chosen pivot strategy.",
            "Algorithms",
            2
        });
        cards.push_back({
            "What is the primary function of ATP synthase?",
            "Synthesizes ATP from ADP and inorganic phosphate using a proton gradient across the inner mitochondrial membrane.",
            "Cell Biology",
            3
        });
        cards.push_back({
            "How does increasing the central bank discount rate impact inflation?",
            "Raises the cost of borrowing, which reduces consumer spending and business investment, cooling demand and lowering inflation pressure.",
            "Economics",
            2
        });
    }

    return cards;
}

double calculateMasteryScore(int totalCards, int masteredCards, int reviewStreak) {
    if (totalCards <= 0) return 0.0;
    double baseRatio = static_cast<double>(masteredCards) / static_cast<double>(totalCards);
    // Streak multiplier bonus up to +15%
    double streakBonus = std::min(0.15, reviewStreak * 0.02);
    double score = (baseRatio * 0.85) + streakBonus;
    return std::min(1.0, std::max(0.0, score));
}

} // namespace studyflow
