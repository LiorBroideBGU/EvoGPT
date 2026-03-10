INPUT_PROMPT = """
<Task>
Generate a JUnit 4.13.2 unit test for the java class below:
</Task>

<SourceCode>
{}
</SourceCode>

<Instruction>
The following methods did not reach the 75% percent branch\line coverage threshold in the current test suite:
{}
Your focus should be on these methods, and on ensuring missed branches are covered.
The following branches were missed in the current test suite. 
These lines contain uncovered conditional logic:
{}
</Instruction>

<Constraint>
- Output ONLY the raw Java code. 
- Do not use markdown backticks (```).
- Do not include any introductory or concluding text.
- Start your response immediately with the 'package' keyword.
</Constraint>

Response:
"""
