package com.strands.interviews.eventsystem;

import com.strands.interviews.eventsystem.events.SimpleEvent;
import com.strands.interviews.eventsystem.events.SubEvent;
import com.strands.interviews.eventsystem.impl.DefaultEventManager;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventManagerTest
{
    private EventManager eventManager = new DefaultEventManager();

    @Test
    public void testPublishNullEvent()
    {
        eventManager.publishEvent(null);
    }

    @Test
    public void testRegisterListenerAndPublishEvent()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.publishEvent(new SimpleEvent(this));
        assertTrue(eventListenerMock.isCalled());
    }

    @Test
    public void testListenerWithoutMatchingEventClass()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SubEvent.class});
        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.publishEvent(new SimpleEvent(this));
        assertFalse(eventListenerMock.isCalled());
    }

    @Test
    public void testUnregisterListener()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock eventListenerMock2 = new EventListenerMock(new Class[]{SimpleEvent.class});

        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.registerListener("another.key", eventListenerMock2);
        eventManager.unregisterListener("some.key");

        eventManager.publishEvent(new SimpleEvent(this));
        assertFalse(eventListenerMock.isCalled());
        assertTrue(eventListenerMock2.isCalled());
    }


    /**
     * Check that registering and unregistering listeners behaves properly.
     */
    @Test
    public void testRemoveNonexistentListener()
    {
        DefaultEventManager dem = (DefaultEventManager)eventManager;
        assertEquals(0, dem.getListeners().size());
        eventManager.registerListener("some.key", new EventListenerMock(new Class[]{SimpleEvent.class}));
        assertEquals(1, dem.getListeners().size());
        eventManager.unregisterListener("this.key.is.not.registered");
        assertEquals(1, dem.getListeners().size());
        eventManager.unregisterListener("some.key");
        assertEquals(0, dem.getListeners().size());
    }

    /**
     * Registering duplicate keys on different listeners should only fire the most recently added.
     */
    @Test
    public void testDuplicateKeysForListeners()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock eventListenerMock2 = new EventListenerMock(new Class[]{SimpleEvent.class});

        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.registerListener("some.key", eventListenerMock2);

        eventManager.publishEvent(new SimpleEvent(this));

        assertTrue(eventListenerMock2.isCalled());
        assertFalse(eventListenerMock.isCalled());

        eventListenerMock.resetCalled();
        eventListenerMock2.resetCalled();

        eventManager.unregisterListener("some.key");
        eventManager.publishEvent(new SimpleEvent(this));

        assertFalse(eventListenerMock2.isCalled());
        assertFalse(eventListenerMock.isCalled());
    }

    /**
     * Attempting to register a null with a valid key should result in an illegal argument exception
     */
    @Test
    public void testAddValidKeyWithNullListener()
    {
        try
        {
            eventManager.registerListener("bogus.key", null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
    
    @Test
    public void testSubEventDoesNotNotifySimpleEventListeners()
    {
    	
    	// Create a SimpleEvent listener
    	EventListenerMock simpleEventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        eventManager.registerListener("some.key", simpleEventListenerMock);
        

        // Create a SubEvent listener
        EventListenerMock subEventListenerMock = new EventListenerMock(new Class[]{SubEvent.class});
        eventManager.registerListener("some.key", subEventListenerMock);
        

        // Create and send a SubEvent
        eventManager.publishEvent(new SubEvent(this));

        // Verify that only the SubEvent listener was called
        assertTrue(subEventListenerMock.isCalled());
        assertFalse(simpleEventListenerMock.isCalled());
    }
    
    @Test
    public void testAggregateListeners() {

        EventListenerMock listenerMock = new EventListenerMock(new Class[] {SimpleEvent.class});
        EventListenerMock subListenerMock = new EventListenerMock(new Class[] {SubEvent.class});

        eventManager.registerListener("simple", listenerMock);
        eventManager.registerListener("sub", subListenerMock);

        SimpleEvent simpleEvent = new SimpleEvent(this);
        eventManager.publishEvent(simpleEvent);
        assertTrue(listenerMock.isCalled());
        assertTrue(subListenerMock.isCalled());

        listenerMock.resetCalled();
        subListenerMock.resetCalled();

        SubEvent subEvent = new SubEvent(this);
        eventManager.publishEvent(subEvent);
        assertFalse(listenerMock.isCalled()); // SimpleEvent listener should not be called
        assertTrue(subListenerMock.isCalled());

        eventManager.unregisterListener("simple");
        eventManager.unregisterListener("sub");
    }
}
