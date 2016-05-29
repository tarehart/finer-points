## Monolithic strategies for publishing edits

### Publish always results in a novel id

Pros:
- Everything automatically immutable, so no vandalism.
- Allowed and encouraged to edit anything at any time.

Cons:
- Number of nodes in database would explode, causing expense and performance problems.
- Users would be forced to choose from many seemingly identical copies of the same node,
differing only because of changes deep in their dependencies.
- Users would be confused when their edit to a child in one tree is not reflected in another.

#### Full Consumer Rebuild

This is like a build system for code packages. Every time an edit is published, all consumers
must be republished also. The new copies of the consumers would have their build version
incremented.

#### Scoped Consumer Rebuild

When an edit is published, only a subset of the consumer graph is published. It is limited to
the nodes that the user was currently viewing in the browser.

### Publish always modifies an existing id

Cons:
- Vandalism possible. Probably need to maintain an edit history.
- Merge conflicts!

Pros:
- Database size kept manageable
- Users have easier time selecting nodes
- Edits in one place are reflected everywhere, as expected. Works like wikipedia.


## Nuanced strategies

### Node permissions model

Permissions apply to nodes, not entire trees.

States:
- Public community wiki
  - Globally visible
  - Globally editable
    - Edit in place
    - History recorded to mitigate vandalism
  - Cannot be converted to managed mode
- Public managed
  - Blessed users may edit in place
  - Excluded users may create a competing copy
    - Make a flow for this which highlights all existing competition. Maybe somebody
    already made a competing wiki that is suitable.
  - Can be opened up to community wiki status
- Private pending edit (any context)
  - Visible only to author

#### Explicitly competing / forked nodes

We may want a sub header where users can express the distinction without changing
the main title. Examples:
- Climate change is real
  - Formal edition
- Climate change is real
  - Fast and loose, possibly insulting

It is legitimate to have competing community wikis. Use the word "fork"?

#### Class design

Parent-child relationships exist among ArgumentNode instances.

ArgumentNode has a ArgumentBody

ArgumentBody has title, subheading, pointer to previous version,
pointer to MajorVersion.