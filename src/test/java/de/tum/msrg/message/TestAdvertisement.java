package de.tum.msrg.message;

import de.tum.msrg.message.Advertisement;
import de.tum.msrg.message.Attribute;
import de.tum.msrg.message.Subscription;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by pxsalehi on 09.02.16.
 */
public class TestAdvertisement {

    @Test
    public void testIsMatches() {
        // a1={0, x > 10, y < 200} a2={1, x < 100, 150 < y < 400}
        // s1={0, x > 0} s2={1, 0 < x < 50, y < 500}, s3={1; y < 10}, s4={1}
        Attribute.noOfAttributes = 2;
        Attribute.lowRange = 0;
        Attribute.highRange = 1000;
        Subscription.setTotalClasses(2);
        Advertisement a1 = new Advertisement(0, new Attribute[]{Attribute.greaterThan(10), Attribute.lessThan(200)});
        Advertisement a2 = new Advertisement(1, new Attribute[]{Attribute.lessThan(100), Attribute.range(150, 400)});
        Subscription s1 = new Subscription(0, new Attribute[]{Attribute.greaterThan(0), Attribute.star()});
        Subscription s2 = new Subscription(1, new Attribute[]{Attribute.range(0, 50), Attribute.lessThan(500)});
        Subscription s3 = new Subscription(1, new Attribute[]{Attribute.star(), Attribute.lessThan(10)});
        Subscription s4 = new Subscription(1, new Attribute[]{Attribute.star(), Attribute.star()});
        assertTrue(a1.isMatches(s1));
        assertFalse(a1.isMatches(s2));
        assertFalse(a1.isMatches(s3));
        assertFalse(a1.isMatches(s4));
        assertFalse(a2.isMatches(s1));
        assertTrue(a2.isMatches(s2));
        assertFalse(a2.isMatches(s3));
        assertTrue(a2.isMatches(s4));
    }
}
