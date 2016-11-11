## Strategy

### Publish always modifies an existing id

Cons:
- Vandalism possible. Probably need to maintain an edit history.
- Merge conflicts!

Pros:
- Database size kept manageable
- Users have easier time selecting nodes
- Edits in one place are reflected everywhere, as expected. Works like wikipedia.

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


### Discarded Strategies

#### Publish always results in a novel id

Pros:
- Everything automatically immutable, so no vandalism.
- Allowed and encouraged to edit anything at any time.

Cons:
- Number of nodes in database would explode, causing expense and performance problems.
- Users would be forced to choose from many seemingly identical copies of the same node,
differing only because of changes deep in their dependencies.
- Users would be confused when their edit to a child in one tree is not reflected in another.

##### Full Consumer Rebuild

This is like a build system for code packages. Every time an edit is published, all consumers
must be republished also. The new copies of the consumers would have their build version
incremented.

##### Scoped Consumer Rebuild

When an edit is published, only a subset of the consumer graph is published. It is limited to
the nodes that the user was currently viewing in the browser.

## Class design

Parent-child relationships exist among ArgumentNode instances.

ArgumentNode has a ArgumentBody

ArgumentBody has title, subheading, pointer to previous version,
pointer to MajorVersion.

### Node-Body separation

I am choosing to keep ArgumentNode and ArgumentBody separate entities to
elegantly support snapshot behavior, which is not yet implemented.
The text below is a record of how I meandered towared that conclusion.

#### Pondering Unification

Why are ArgumentNodes modeled as separate entities from ArgumentBodies?
Based on the currently available APIs in the ArgumentService, they maintain a 1:1
correspondence. It is vanishingly rare that we want to query for one without the other.
Having them separate increases complexity, decreases performance of database queries,
and makes json payloads slightly heavier.

They were originally designed to be separate when I was working toward the
"Publish always results in a novel id" strategy. The strategy would cause edits
to propagate outward, creating clones with different new parent-child
connectivity throughout the graph. Since these clones would have zero change to
title or content, I chose to have the "body" be a separate entity that could be
referenced by many nodes. This would reduce storage requirements and data duplication.

Now that I'm generally doing Wikipedia style editing, the original impetus for
this design is gone. I think I came up with a reason for wanting to keep it anyway,
but at the moment I don't recall what it is.

Let's consider the node-body separation to be a candidate for deprecation. I will
leave it as-is for now to give myself time to remember why it may still be useful.
Something to do with snapshots? Child editing? Who knows.

Invalid arguments:
* ArgumentNodes are the things with public-facing urls, and I want those to be
able to float and point to the next ArgumentBody when a publish happens
  * This offers no advantage over simply doing a content swap between the draft
  node and the public node during publish.

Note to self: be careful with client side node caching because nodes are mutable now.
Consider always making the ajax call, and only using the cache for the snappy,
momentary display of potentially old stuff.

#### Remembering Snapshots

Let's talk about history. What do you want the view to be? A list of items.
Click on an item, what do you get? How about a title and a body, but no
strong links to children. Totally cut off from the parent-child graph.
However, we do have major version ids, which is adequate for providing the user
a link to the latest public instance in that series. Nothing here is really a
strong argument for or against node-body separation. If they're separate, then it's
just a linked list of bodies. If they're unified, it's a linked list of nodes
that have had their relationships trimmed off.

Regarding unification, if we do that, then what's the point of major version nodes?
Just have a mondo node with a linked list of bodies trailing off of it, and have it
link to a VersionAggregator. If we do that, what does snapshot functionality look like?
If a snapshot is done by making new mondo nodes, we're going to have a tricky situation
with votes and comments.

For snapshots, I think the ideal thing would be to not unify. The linked list of bodies
would have a few of the bodies linked to nodes which are snapshots. The point of the
node is to give the thing an addressable stableId and to provide the proper
connectivity to shapshotted children.

Can we say that snapshots are completely immutable? That would turn into a real mess
when you're looking for a child to link to. If you really need to fork, make a
new major version. Forking abandons all comments and votes.