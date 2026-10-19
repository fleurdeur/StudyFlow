#include "native.h"
#include <regex>
#include <algorithm>

namespace studyflow {

CategorizationResult categorizeContent(const std::string& title, const std::string& content) {
    CategorizationResult result;
    result.courseCode = "GEN100";
    result.topic = "General Studies";
    result.category = "Academics";
    result.confidence = 0.50;

    std::string combined = title + " " + content;
    std::string lowerCombined = combined;
    std::transform(lowerCombined.begin(), lowerCombined.end(), lowerCombined.begin(), ::tolower);

    // Regex for standard course codes: 2-4 uppercase letters followed by 3 digits (e.g. CS201, BIO105, MATH220)
    std::regex courseRegex(R"(([A-Z]{2,4})\s*([0-9]{3}))");
    std::smatch match;
    if (std::regex_search(combined, match, courseRegex)) {
        result.courseCode = match.str(1) + match.str(2);
        result.confidence = 0.95;
    }

    // Keyword based topic categorization
    if (lowerCombined.find("algorithm") != std::string::npos ||
        lowerCombined.find("binary tree") != std::string::npos ||
        lowerCombined.find("data structure") != std::string::npos ||
        lowerCombined.find("compiler") != std::string::npos ||
        lowerCombined.find("recursion") != std::string::npos) {
        if (result.courseCode == "GEN100") result.courseCode = "CS201";
        result.topic = "Data Structures & Algorithms";
        result.category = "Computer Science";
        result.confidence = std::max(result.confidence, 0.92);
    } else if (lowerCombined.find("mitochondria") != std::string::npos ||
               lowerCombined.find("atp") != std::string::npos ||
               lowerCombined.find("respiration") != std::string::npos ||
               lowerCombined.find("cell") != std::string::npos ||
               lowerCombined.find("photosynthesis") != std::string::npos) {
        if (result.courseCode == "GEN100") result.courseCode = "BIO105";
        result.topic = "Cellular Respiration & Bioenergetics";
        result.category = "Biology";
        result.confidence = std::max(result.confidence, 0.94);
    } else if (lowerCombined.find("inflation") != std::string::npos ||
               lowerCombined.find("interest rate") != std::string::npos ||
               lowerCombined.find("macroeconomic") != std::string::npos ||
               lowerCombined.find("monetary policy") != std::string::npos ||
               lowerCombined.find("gdp") != std::string::npos) {
        if (result.courseCode == "GEN100") result.courseCode = "ECON101";
        result.topic = "Macroeconomics & Monetary Policy";
        result.category = "Economics";
        result.confidence = std::max(result.confidence, 0.91);
    } else if (lowerCombined.find("integral") != std::string::npos ||
               lowerCombined.find("derivative") != std::string::npos ||
               lowerCombined.find("matrix") != std::string::npos ||
               lowerCombined.find("calculus") != std::string::npos) {
        if (result.courseCode == "GEN100") result.courseCode = "MATH220";
        result.topic = "Multivariable Calculus";
        result.category = "Mathematics";
        result.confidence = std::max(result.confidence, 0.90);
    } else if (lowerCombined.find("covalent") != std::string::npos ||
               lowerCombined.find("reaction") != std::string::npos ||
               lowerCombined.find("organic chemistry") != std::string::npos) {
        if (result.courseCode == "GEN100") result.courseCode = "CHEM101";
        result.topic = "Chemical Bonding & Reactions";
        result.category = "Chemistry";
        result.confidence = std::max(result.confidence, 0.88);
    }

    return result;
}

} // namespace studyflow
