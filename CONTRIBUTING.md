# Developing Public API Strangler

## Making a change

Master should always have the version currently deployed. It is fine to commit directly to master and make sure that jenkins is always green and deployed. PR's are mostly used for asking for feedback, comments and external contributions, not as a hard requirement to ship code. It is fine to put something in production and then, in a separate commit, address stylistic and non blocking issues.

For external contributions (coming from non mantainers of this system) to these projects, as Service Custodians we (core-services) kindly ask people to open PR's or pair with one of us in order to make sure that these contributions aligns with the plans and the idioms in this codebase and our release schedules.


## Local environment

Depends on boot2docker and docker-compose being setup and working properly.

make precheckin (run all the integration and unit tests. Run this before commiting)
