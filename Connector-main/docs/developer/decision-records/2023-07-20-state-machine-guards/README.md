# State Machine guards

## Decision

The EDC will provide a way to permit external interactions over internal (automatic) ones in the state machine processes through guards.

## Rationale

In the EDC domain, the state machine is currently a completely automatic engine, every state transition is pre-defined
and they are following completely automatic logic.

With the upcoming introduction of the so-called "counter offer" feature, there's the need to permit to avoid the state machine to
pick up certain states and having the user interacting with it through commands in the Management API.

This will be permitted in the most generic way possible, giving to every state machine the possibility to "interrupt" the
automatic flow and let the user act on it.

## Approach

This implementation is based on two pillars:
- adding a generic guard functionality to the state machine
- add a flag that permits to recognize entities that are `pending` (waiting for external interactions)

The first one will be implemented in the `StateProcessorImpl`, that's the component that executes the state machine logic.
In the new implementation, there will be the possibility to add a `Guard`, that's a tuple of a `Predicate` and a `Function`.
The new `StateProcessorImpl` flow will be:
```java
entities.get().stream()
    .map(entity -> {
        if (hook.predicate().test(entity)) {
            return hook.process().apply(entity);
        } else {
            return process.apply(entity);
        }
    })
    .filter(isEqual(true))
    .count()
```

so the `Guard` will take over in the case its predicate matches the entity.

This way it will be possible to control the flow, and every `*Manager` (`ContractNegotiation`, `TransferProcess`, ...) 
can then register their own `PendingGuard` on the state machine configuration.
In particular, the default hook function implementation will set the "pending" flag on the entity,
like: 
```
Function<Entity> hookFunction = entity -> {
    entity.setPending(true);
    update(entity);
    return true;
```

The flag will be then used as an additional filter passed to the `store.nextNotLeased` method used by the state machine
to filter out such entities. This way they will just stand still in the database, pending.

The hook predicate will be completely extensible and will permit the implementors to decide if a specific entity state needs
external interaction based on the input, like:
```java
class EntityPendingGuard implements PendingGuard<Entity> {
    
    // custom collaborators as other services
    
    boolean test(Entity entity) {
        // custom condition
        return entity.getState() = SPECIFIC_STATE.code() && otherCondition; // if true, the entity will be pending
    }
    
}
```
