package com.strands.interviews.eventsystem.impl;

import com.strands.interviews.eventsystem.EventManager;
import com.strands.interviews.eventsystem.InterviewEvent;
import com.strands.interviews.eventsystem.InterviewEventListener;

import java.util.*;

/**
 * Manages the firing and receiving of events.
 *
 * <p>Any event passed to {@link #publishEvent} will be passed through to "interested" listeners.
 *
 * <p>Event listeners can register to receive events via
 * {@link #registerListener(String, com.strands.interviews.eventsystem.InterviewEventListener)}
 */
public class DefaultEventManager implements EventManager
{
    private Map listeners = new HashMap();
    private Map listenersByClass = new HashMap();
    private List<InterviewEventListener> allEventListeners = new ArrayList<>();
    private Map<Class<? extends InterviewEvent>, List<InterviewEventListener>> listenersMap;
 
    public void publishEvent(InterviewEvent event)
    {
        if (event == null)
        {
            System.err.println("Null event fired?");
            return;
        }

        sendEventTo(event, calculateListeners(event.getClass()));
    }

    private Collection calculateListeners(Class eventClass)
    {
        return (Collection) listenersByClass.get(eventClass);
    }


    public void registerListener(String listenerKey, InterviewEventListener listener) {
        if (listenerKey == null || listenerKey.isEmpty())
            throw new IllegalArgumentException("Key for the listener must not be null or empty");

        if (listener == null)
            throw new IllegalArgumentException("The listener must not be null");

        if (listeners.containsKey(listenerKey))
            unregisterListener(listenerKey);

        listeners.put(listenerKey, listener);

        // Find all existing listeners that handle a superclass of the new listener's classes
        for (InterviewEventListener existingListener : listeners.values()) {
            Class<?>[] existingClasses = existingListener.getHandledEventClasses();
            Class<?>[] newClasses = listener.getHandledEventClasses();

            for (Class<?> existingClass : existingClasses) {
                for (Class<?> newClass : newClasses) {
                    if (existingClass.isAssignableFrom(newClass)) {
                        addToListenerList(existingClass, listener);
                    }
                }
            }
        }

        // Add the new listener to the appropriate listener list for each of its classes
        for (Class<?> clazz : listener.getHandledEventClasses()) {
            addToListenerList(clazz, listener);
        }
    }

    public void unregisterListener(String listenerKey)
    {
        InterviewEventListener listener = (InterviewEventListener) listeners.get(listenerKey);

        for (Iterator it = listenersByClass.values().iterator(); it.hasNext();)
        {
            List list = (List) it.next();
            list.remove(listener);
        }

        listeners.remove(listenerKey);
    }

    private void sendEventTo(InterviewEvent event, Collection listeners)
    {
        if (listeners == null || listeners.size() == 0)
            return;

        for (Iterator it = listeners.iterator(); it.hasNext();)
        {
            InterviewEventListener eventListener = (InterviewEventListener) it.next();
            eventListener.handleEvent(event);
        }
    }

    private void addToListenerList(Class aClass, InterviewEventListener listener)
    {
        if (!listenersByClass.containsKey(aClass))
            listenersByClass.put(aClass, new ArrayList());

        ((List)listenersByClass.get(aClass)).add(listener);
    }

    public Map getListeners()
    {
        return listeners;
    }
    
    private void handleAllEvents(InterviewEvent event) {
        for (InterviewEventListener listener : allEventListeners) {
            listener.handleEvent(event);
        }
    }
    
    @Override
    public List<InterviewEventListener> getListenersForEvent(Class<? extends InterviewEvent> eventClass) {
        // return the list of listeners for the event class and its superclasses
        List<InterviewEventListener> listeners = new ArrayList<>();
        Class<?> clazz = eventClass;
        while (clazz != null) {
            List<InterviewEventListener> listenersForClass = listenersMap.get(clazz);
            if (listenersForClass != null) {
                listeners.addAll(listenersForClass);
            }
            clazz = clazz.getSuperclass();
        }
        return listeners;
    }
    
}
