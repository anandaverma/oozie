/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oozie.event;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.oozie.client.event.Event;
import org.apache.oozie.util.XLog;

/**
 * An implementation of the EventQueue, defining a memory-based data structure
 * holding the events
 */
public class MemoryEventQueue implements EventQueue {

    private static ConcurrentLinkedQueue<EventQueueElement> eventQueue;
    private static AtomicInteger currentSize;
    private static int maxSize;
    private static XLog LOG;
    private static int batchSize;

    @Override
    public void init(int queueSize, int batchsize) {
        eventQueue = new ConcurrentLinkedQueue<EventQueueElement>();
        maxSize = queueSize;
        currentSize = new AtomicInteger();
        batchSize = batchsize;
        LOG = XLog.getLog(getClass());
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public void add(Event e) {
        EventQueueElement eqe = new EventQueueElement(e);
        try {
            if(getCurrentSize() <= maxSize) {
                if(eventQueue.add(eqe)) {
                    currentSize.incrementAndGet();
                }
            }
            else {
                LOG.warn("Queue size [{0}] reached max limit. Element [{1}] not added", getCurrentSize(), e);
            }
        }
        catch (IllegalStateException ise) {
            LOG.warn("Unable to add event due to " + ise);
        }
    }

    @Override
    public Set<Event> pollBatch() {
        // batch drain
        Set<Event> eventBatch = new HashSet<Event>();
        for (int i = 0; i < batchSize; i++) {
            EventQueueElement polled = eventQueue.poll();
            if (polled != null) {
                currentSize.decrementAndGet();
                eventBatch.add(polled.event);
            }
            else {
                LOG.debug("Current queue size [{0}] less than polling batch size [{1}]", currentSize.get(), batchSize);
                break;
            }
        }
        return eventBatch;
    }

    @Override
    public Event poll() {
        EventQueueElement polled = eventQueue.poll();
        if (polled != null) {
            currentSize.decrementAndGet();
            return polled.event;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return getCurrentSize() == 0;
    }

    @Override
    public int getCurrentSize() {
        return currentSize.intValue();
    }

    @Override
    public Event peek() {
        EventQueueElement peeked = eventQueue.peek();
        if (peeked != null) {
            return peeked.event;
        }
        return null;
    }

}
