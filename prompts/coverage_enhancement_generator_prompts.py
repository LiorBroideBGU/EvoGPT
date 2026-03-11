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

Response:
"""
