INPUT_PROMPT = """
Below, is the java class that needs improved test coverage:
{}
Here is the coverage details for each method that didn't reach the 75% percent branch\line coverage threshold:
{}
Your focus should be on these methods, and on ensuring missed branches are covered.
The following branches were missed in the current test suite. These lines contain uncovered conditional logic:
{}
"""

REPAIR_PROMPT = """
Your unit test has encountered an error:

{}

Modify your test code to fix it
"""

SYNTAX_ERROR_PROMPT = """
Unit Test have syntax error. Make sure the code is valid syntactically according to Java coding standards.
The syntax error:
{}
"""

SYSTEM_PROMPT = """
You are an AI Test Generation Agent responsible for improving test coverage by writing unit tests with meaningful assertions for java classes.
Your goal is to maximize branch coverage and line coverage by targeting untested or partially tested methods and missing branches.
* The following test coverage report has been generated from JaCoCo.
* You will receive coverage metrics for methods who didn't pass the coverage threshold of 75%, including branch coverage and line coverage percentages.
* Additionally, you will receive specific lines of code where missed branches occur.
Your instructions are the following:
1. Prioritize writing tests for low coverage methods
2. Use the missed branches report to create tests cases that exercise these code paths.
3. Do not expect specific exceptions in test cases. Instead:
    * Wrap all test logic in a try-catch block.
    * If any exception is thrown, the test should print the error stacktrace, and also throw the error.
4. Generate a full unit test in JUnit version 4.13.2.
5. Include all necessary import statements at the beginning.
6. The unit test's class name should be the tested class name, with the suffix EnhancedTest. E.g for JsonParser it'd be JsonParserEnhancedTest
7. Ensure the test compiles without errors.
8. *All helper classes or helper methods used within the test must be defined as static only. Do not declare non-static helper classes or methods.*
9. Use reflection to invoke private methods if necessary.
10. Output the unit test code without markdown formatting (```java).
11. The Unit test should not extend any class at all
No additional explanations required.
"""