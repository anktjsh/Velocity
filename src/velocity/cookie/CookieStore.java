/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.cookie;

/**
 *
 * @author Aniket
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import velocity.util.FileUtils;

/**
 * A cookie store.
 */
final class CookieStore {

    private static final Logger logger
            = Logger.getLogger(CookieStore.class.getName());

    private static final int MAX_BUCKET_SIZE = 50;
    private static final int TOTAL_COUNT_LOWER_THRESHOLD = 3000;
    private static final int TOTAL_COUNT_UPPER_THRESHOLD = 4000;

    /**
     * The mapping from domain names to cookie buckets. Each cookie bucket
     * stores the cookies associated with the corresponding domain. Each cookie
     * bucket is represented by a Map<Cookie,Cookie> to facilitate retrieval of
     * a cookie by another cookie with the same name, domain, and path.
     */
    private final Map<String, Map<Cookie, Cookie>> buckets
            = new HashMap<>();

    /**
     * The total number of cookies currently in the store.
     */
    private int totalCount = 0;

    /**
     * Creates a new {@code CookieStore}.
     */
    CookieStore() {
    }

    public void remove(Cookie c) {
        if (buckets.keySet().contains(c.getDomain())) {
            if (buckets.get(c.getDomain()).get(c) != null) {
                buckets.get(c.getDomain()).remove(c);
            }
        }
    }

    List<Cookie> get() {
        ArrayList<Cookie> al = new ArrayList<>();
        for (String s : buckets.keySet()) {
            Map<Cookie, Cookie> map = buckets.get(s);
            for (Cookie c : map.keySet()) {
                al.add(c);
            }
        }
        return al;
    }

    /**
     * Returns the currently stored cookie with the same name, domain, and path
     * as the given cookie.
     */
    Cookie get(Cookie cookie) {
        Map<Cookie, Cookie> bucket = buckets.get(cookie.getDomain());
        if (bucket == null) {
            return null;
        }
        Cookie storedCookie = bucket.get(cookie);
        if (storedCookie == null) {
            return null;
        }
        if (storedCookie.hasExpired()) {
            bucket.remove(storedCookie);
            totalCount--;
            log("Expired cookie removed by get", storedCookie, bucket);
            return null;
        }
        return storedCookie;
    }

    /**
     * Returns all the currently stored cookies that match the given query.
     */
    List<Cookie> get(String hostname, String path, boolean secureProtocol,
            boolean httpApi) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "hostname: [{0}], path: [{1}], "
                    + "secureProtocol: [{2}], httpApi: [{3}]", new Object[]{
                        hostname, path, secureProtocol, httpApi});
        }

        ArrayList<Cookie> result = new ArrayList<>();

        String domain = hostname;
        while (domain.length() > 0) {
            Map<Cookie, Cookie> bucket = buckets.get(domain);
            if (bucket != null) {
                find(result, bucket, hostname, path, secureProtocol, httpApi);
            }
            int nextPoint = domain.indexOf('.');
            if (nextPoint != -1) {
                domain = domain.substring(nextPoint + 1);
            } else {
                break;
            }
        }

        Collections.sort(result, new GetComparator());

        long currentTime = System.currentTimeMillis();
        for (Cookie cookie : result) {
            cookie.setLastAccessTime(currentTime);
        }

        logger.log(Level.FINEST, "result: {0}", result);
        return result;
    }

    /**
     * Finds all the cookies that are stored in the given bucket and match the
     * given query.
     */
    private void find(List<Cookie> list, Map<Cookie, Cookie> bucket,
            String hostname, String path, boolean secureProtocol,
            boolean httpApi) {
        Iterator<Cookie> it = bucket.values().iterator();
        while (it.hasNext()) {
            Cookie cookie = it.next();
            if (cookie.hasExpired()) {
                it.remove();
                totalCount--;
                log("Expired cookie removed by find", cookie, bucket);
                continue;
            }

            if (cookie.getHostOnly()) {
                if (!hostname.equalsIgnoreCase(cookie.getDomain())) {
                    continue;
                }
            } else if (!Cookie.domainMatches(hostname, cookie.getDomain())) {
                continue;
            }

            if (!Cookie.pathMatches(path, cookie.getPath())) {
                continue;
            }

            if (cookie.getSecureOnly() && !secureProtocol) {
                continue;
            }

            if (cookie.getHttpOnly() && !httpApi) {
                continue;
            }

            list.add(cookie);
        }
    }

    private static final class GetComparator implements Comparator<Cookie> {

        @Override
        public int compare(Cookie c1, Cookie c2) {
            int d = c2.getPath().length() - c1.getPath().length();
            if (d != 0) {
                return d;
            }
            return c1.getCreationTime().compareTo(c2.getCreationTime());
        }
    }

    /**
     * Stores the given cookie.
     */
    void put(Cookie cookie) {
        Map<Cookie, Cookie> bucket = buckets.get(cookie.getDomain());
        if (bucket == null) {
            bucket = new LinkedHashMap<>(20);
            buckets.put(cookie.getDomain(), bucket);
        }
        if (cookie.hasExpired()) {
            log("Cookie expired", cookie, bucket);
            if (bucket.remove(cookie) != null) {
                totalCount--;
                log("Expired cookie removed by put", cookie, bucket);
            }
        } else if (bucket.put(cookie, cookie) == null) {
            totalCount++;
            log("Cookie added", cookie, bucket);
            if (bucket.size() > MAX_BUCKET_SIZE) {
                purge(bucket);
            }
            if (totalCount > TOTAL_COUNT_UPPER_THRESHOLD) {
                purge();
            }
        } else {
            log("Cookie updated", cookie, bucket);
        }
    }

    public void save() {
        TreeMap<String, List<List<String>>> map = new TreeMap<>();
        for (String uri : buckets.keySet()) {
            if (map.get(uri) == null) {
                map.put(uri, new ArrayList<>());
            }
            Map<Cookie, Cookie> cookie = buckets.get(uri);
            for (Cookie c : cookie.keySet()) {
                map.get(uri).add(
                        FXCollections.observableArrayList(
                                c.getName(),
                                c.getValue(),
                                c.getExpiryTime() + "",
                                c.getDomain(),
                                c.getPath(),
                                c.getCreationTime().baseTime() + "",
                                c.getCreationTime().subtime() + "",
                                c.getLastAccessTime() + "",
                                c.getPersistent() + "",
                                c.getHostOnly() + "",
                                c.getSecureOnly() + "",
                                c.getHttpOnly() + ""
                        ));
            }
        }
        File f = FileUtils.newFile("cookies.txt");
        ArrayList<String> al = new ArrayList<>();
        for (String u : map.keySet()) {
            for (List<String> list : map.get(u)) {
                al.add(u + "=" + list);
            }
        }
        FileUtils.write(f, al);
    }

    public void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(FileUtils.newFile("cookies.txt")));
        for (String s : al) {
            String oper = s.substring(s.indexOf("=") + 1, s.lastIndexOf("]") + 1);
            if (oper.contains("[")) {
                oper = oper.substring(1, oper.length() - 1);
                String[] spl = oper.split(", ");
                if (spl.length > 0) {
                    if (!spl[0].isEmpty()) {
                        try {
                            Cookie c = new Cookie(spl[0],
                                    spl[1],
                                    Long.parseLong(spl[2]),
                                    spl[3],
                                    spl[4],
                                    new ExtendedTime(Long.parseLong(spl[5]), Integer.parseInt(spl[6])),
                                    Long.parseLong(spl[7]),
                                    Boolean.parseBoolean(spl[8]),
                                    Boolean.parseBoolean(spl[9]),
                                    Boolean.parseBoolean(spl[10]),
                                    Boolean.parseBoolean(spl[11]));
                            put(c);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes excess cookies from a given bucket.
     */
    private void purge(Map<Cookie, Cookie> bucket) {
        logger.log(Level.FINEST, "Purging bucket: {0}", bucket.values());

        Cookie earliestCookie = null;
        Iterator<Cookie> it = bucket.values().iterator();
        while (it.hasNext()) {
            Cookie cookie = it.next();
            if (cookie.hasExpired()) {
                it.remove();
                totalCount--;
                log("Expired cookie removed", cookie, bucket);
            } else if (earliestCookie == null || cookie.getLastAccessTime()
                    < earliestCookie.getLastAccessTime()) {
                earliestCookie = cookie;
            }
        }
        if (bucket.size() > MAX_BUCKET_SIZE) {
            bucket.remove(earliestCookie);
            totalCount--;
            log("Excess cookie removed", earliestCookie, bucket);
        }
    }

    /**
     * Removes excess cookies globally.
     */
    private void purge() {
        logger.log(Level.FINEST, "Purging store");

        Queue<Cookie> removalQueue = new PriorityQueue<>(totalCount / 2,
                new RemovalComparator());

        for (Map.Entry<String, Map<Cookie, Cookie>> entry : buckets.entrySet()) {
            Map<Cookie, Cookie> bucket = entry.getValue();
            Iterator<Cookie> it = bucket.values().iterator();
            while (it.hasNext()) {
                Cookie cookie = it.next();
                if (cookie.hasExpired()) {
                    it.remove();
                    totalCount--;
                    log("Expired cookie removed", cookie, bucket);
                } else {
                    removalQueue.add(cookie);
                }
            }
        }

        while (totalCount > TOTAL_COUNT_LOWER_THRESHOLD) {
            Cookie cookie = removalQueue.remove();
            Map<Cookie, Cookie> bucket = buckets.get(cookie.getDomain());
            if (bucket != null) {
                bucket.remove(cookie);
                totalCount--;
                log("Excess cookie removed", cookie, bucket);
            }
        }
    }

    private static final class RemovalComparator implements Comparator<Cookie> {

        @Override
        public int compare(Cookie c1, Cookie c2) {
            return (int) (c1.getLastAccessTime() - c2.getLastAccessTime());
        }
    }

    /**
     * Logs a cookie event.
     */
    private void log(String message, Cookie cookie,
            Map<Cookie, Cookie> bucket) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "{0}: {1}, bucket size: {2}, "
                    + "total count: {3}",
                    new Object[]{message, cookie, bucket.size(), totalCount});
        }
    }
}
