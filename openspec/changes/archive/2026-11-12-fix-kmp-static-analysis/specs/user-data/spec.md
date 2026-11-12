## ADDED Requirements

### Requirement: Favourite changes return a typed result
Adding or removing a favourite SHALL return a typed result (`EmptyResult<DataError.Local>`) instead of throwing, so that
the caller has to handle a failure of the user database (Notion N-17). A failed change SHALL leave the stored
favourites as they were.

#### Scenario: Change succeeds
- **WHEN** a favourite is added or removed and the user database accepts the write
- **THEN** the result is a success and the favourites show the change

#### Scenario: Database write fails
- **WHEN** the user database cannot write, for example because its table is unusable
- **THEN** the result is `DataError.Local.UNKNOWN` and nothing is thrown
