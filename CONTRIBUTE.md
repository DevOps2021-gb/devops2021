# Contributing
When contributing to this repository, please first discuss the change you wish to make via issue,
email, or any other method with the other maintainers of this repository before making a change. 

## Pull Request Process
1. Ensure any install or build dependencies are not part of any commit (not pushed to remote).
2. Update the README.md with details of changes to the interface, this includes new environment 
   variables, exposed ports, useful file locations and container parameters.
3. You may merge the Pull Request in once you have the sign-off of another developer

## Which repository setup will we use?
Mono repository
Simple application as it does not make sense creating a repository for just 4 HTML files

## Which branching model will we use?
* Master: Working releases. Only  pull requests from develop branch
* Develop: Only branch that gets merged into master. Use pull requests when mergin into this. Branch out from this when developing features
* Features: Experiments are allowed and merged with develop when finished

## Which distributed development workflow will we use?
Centralized workflow as the other models does not make sense. this is a small project and team

## How do we expect contributions to look like?
Follow the guidelines listed below (from <https://chris.beams.io/posts/git-commit/>)
1. Separate subject from body with a blank line
2. Capitalize the subject line
3. Use the imperative mood in the subject line
4. Use the body to explain what and why vs. how

## Who is responsible for integrating/reviewing contributions?
Peer review when creating pull requests (you must get your code reviewed by at least on other person)

Example commit message (multiple changes):

<em>
Simplify serialize.h's exception handling


Remove the 'state' and 'exceptmask' from serialize.h's stream implementations, as well as related methods.

As exceptmask always included 'failbit', and setstate was always called with bits = failbit, all it did was immediately raise an
exception. Get rid of those variables, and replace the setstate with direct exception throwing (which also removes some dead code).

As a result, good() is never reached after a failure (there are only 2 calls, one of which is in tests), and can just be replaced by !eof().

fail(), clear(n) and exceptions() are just never called. Delete them.</em>
