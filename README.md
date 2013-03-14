# Hazelcast Round-Robin Selector

This is a basic component for doing non-authoritative round-robining between Hazelcast cluster instances. This can
be useful both as a producer and a consumer for handling balancing of work in a "nearly fair" model.

For a consumer, basic usage looks like this:

```java
HazelcastInstance instance = // ...
RoundRobinSelector selector = new RoundRobinSelector(instance, "selector-name");
Member member = selector.select();
if(member.equals(instance.getCluster().getLocalMember()) {
 // ...
}
```

This is most useful if your application advances the selector by some different production (such as a job being added
to a worker queue).

To advance the selector, you simply do this:

```java
selector.advance();
```

It's often useful when you wish to "push" the work to a particular member that you do something like this:

```java
Something something = produceSomething();
Member member = selector.advance().select();
String uuid = member.getUuid();
ITopic<Something> topic = instance.getTopic("worker-channel-" + uuid);
topic.publish(something);
```