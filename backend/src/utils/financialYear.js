/**
 * Calculates financial year from a given date.
 * Financial year in India runs from April 1 to March 31.
 * Example: May 2026 -> "26-27"
 * Example: Feb 2027 -> "26-27"
 * 
 * @param {Date} date 
 * @returns {string} e.g. "26-27"
 */
const getFinancialYear = (date = new Date()) => {
  const month = date.getMonth(); // 0-indexed (0 = Jan, 3 = Apr)
  const year = date.getFullYear();
  
  let startYear = year;
  let endYear = year + 1;
  
  // If month is Jan, Feb, Mar (0, 1, 2), then it belongs to previous year's FY
  if (month < 3) {
    startYear = year - 1;
    endYear = year;
  }
  
  const startStr = startYear.toString().slice(-2);
  const endStr = endYear.toString().slice(-2);
  
  return `${startStr}-${endStr}`;
};

module.exports = {
  getFinancialYear
};
