/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.blogix.tests.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.mindengine.blogix.components.Post;
import net.mindengine.blogix.db.Entry;
import net.mindengine.blogix.db.EntryList;
import net.mindengine.blogix.db.FileDb;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileDbAccTest {

    private FileDb<Post> postDb;
    
    @BeforeClass
    public void init() throws Exception {
        postDb = new FileDb<Post>(Post.class, new File(getClass().getResource("/test-db/posts/").toURI()));
    }
    
    @Test
    public void shouldGiveNullForUnexistentEntry() throws Exception {
        Entry entry = postDb.findEntryById("2012-01-30-some-unexistent-entry");
        assertThat(entry, is(nullValue()));
    }
    
    @Test
    public void shouldFindExistentEntry() throws Exception {
        Entry entry = postDb.findEntryById("2012-01-30-some-title");
        assertThat(entry, is(notNullValue()));
    }
    
    @Test
    public void shouldFindOnlyIdsForAllEntries() throws Exception {
        EntryList<String> ids = postDb.findAllIds();
        assertThat(ids.size(), is(4));
        assertThat(ids.get(0), is("2012-01-30-some-title"));
        assertThat(ids.get(1), is("2012-02-01-some-title-2"));
        assertThat(ids.get(2), is("2012-03-02-some-title-3"));
        assertThat(ids.get(3), is("2012-03-03-some-title-4"));
    }
    
    @Test
    public void shouldFindAllEntries() throws Exception {
        EntryList<Entry> entries = postDb.findAllEntries();
        assertThat(entries.size(), is(4));
        assertThat(entries.get(0).id(), is("2012-01-30-some-title"));
        assertThat(entries.get(1).id(), is("2012-02-01-some-title-2"));
        assertThat(entries.get(2).id(), is("2012-03-02-some-title-3"));
        assertThat(entries.get(3).id(), is("2012-03-03-some-title-4"));
    }
    
    @Test
    public void shouldFindOnlyIdsBySpecifiedIdRegexPattern() throws Exception {
        EntryList<String> ids = postDb.findAllIds("2012-(02|03).*");
        assertThat(ids.size(), is(3));
        assertThat(ids.get(0), is("2012-02-01-some-title-2"));
        assertThat(ids.get(1), is("2012-03-02-some-title-3"));
        assertThat(ids.get(2), is("2012-03-03-some-title-4"));
    }
    
    @Test
    public void shouldFindEntriesBySpecifiedIdRegexPattern() throws Exception {
        EntryList<Entry> entries = postDb.findAllEntries("2012-(02|03).*");
        assertThat(entries.size(), is(3));
        assertThat(entries.get(0).id(), is("2012-02-01-some-title-2"));
        assertThat(entries.get(1).id(), is("2012-03-02-some-title-3"));
        assertThat(entries.get(2).id(), is("2012-03-03-some-title-4"));
    }
    
    @Test
    public void shouldFindEntryAttachments() throws Exception {
        EntryList<String> attachments =  postDb.findAttachments("2012-01-30-some-title");
        assertThat(attachments.size(), is(2));
        assertThat(attachments.get(0), is("1.jpg"));
        assertThat(attachments.get(1), is("2.png"));
    }
    
    @Test
    public void shouldLoadSimpleEntryInSpecifiedPath() throws Exception {
        Entry entry = postDb.findEntryById("2012-01-30-some-title");
        assertThat(entry.id(), is("2012-01-30-some-title"));
        assertThat(entry.field("title"), is("Sample title"));
        assertThat(entry.field("sections"), is("Section 1, Section 2, Section 3"));
        assertThat(entry.body(), is("This is just a body\n---------------This is actually not a delimiter"));
        assertThat(entry.field("anotherField"), is("field value after body"));
        assertThat(entry.field("someUnexistentField"), is(nullValue()));
    }
    
    @Test
    public void shouldLoadSimpleEntryAndMapToJavaClass() throws Exception {
        assertFirstPost(postDb.findById("2012-01-30-some-title"));
    }
    
    @Test
    public void shouldFindAllEntriesAndMapToJavaClasses() throws Exception {
        EntryList<Post> posts = postDb.findAll();
        assertThat(posts.size(), is(4));
        assertFirstPost(posts.get(0));
    }
    
    @Test
    public void shouldGiveNull_toArrayType_ifItWasEmpty_inEntryFile() throws Exception {
        Post post = postDb.findById("2012-03-03-some-title-4");
        assertThat(post, is(notNullValue()));
        assertThat(post.getSections(), is(new String[]{}));
    }
    
    private void assertFirstPost(Post post) {
        assertThat(post.getTitle(), is("Sample title"));
        assertThat(post.getId(), is("2012-01-30-some-title"));
        assertThat(post.getSections(), is(notNullValue()));
        assertThat(Arrays.asList(post.getSections()), Matchers.contains("Section 1", "Section 2", "Section 3"));
        assertThat(post.getBody(), is("This is just a body\n---------------This is actually not a delimiter"));
        assertThat(post.getCommentsEnabled(), is(true));
    }

    @Test
    public void shouldSearchOnContainingField() throws Exception {
        List<Entry> entries = postDb.findEntriesByFieldContaining("sections", "Section 1");
        assertThat(entries.size(), is(2));
        assertThat(entries.get(0).id(), is("2012-01-30-some-title"));
        assertThat(entries.get(1).id(), is("2012-02-01-some-title-2"));        
    }
    
    
}
