package net.mindengine.blogix.tests.blog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.blog.controllers.Blogix;
import net.mindengine.blogix.model.Post;
import net.mindengine.blogix.model.Section;

import org.testng.annotations.Test;

public class BlogixControllerTest {
    
    private static final String TITLE_BASE = " | Blogix Blog";
    private static final String TITLE = "title";
    private static final int NUMBER_OF_ALL_POSTS_FOR_SECTION_1 = 12;
    private static final int NUMBER_OF_ALL_POSTS_FOR_SECTION_2 = 2;
    private static final int NUMBER_OF_SECOND_PAGE_POSTS_IN_TEST = 4;
    private static final int DEFAULT_NUMBER_OF_RECENT_POSTS = 5;
    private static final int NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST = 10;
    private static final int NUMBER_OF_ALL_POSTS_IN_TEST = 14;
    private static final String RECENT_POSTS = "recentPosts";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static final String HOME_POSTS = "homePosts";
    private static final String CURRENT_PAGE = "currentPage";
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void homePageShouldProvideFirstPagePosts() throws Exception {
        Map<String, Object> homePageModel = Blogix.homeFirstPage();
        
        assertCommonModelDataForPosts(homePageModel);
        
        assertThat(homePageModel,hasKey(HOME_POSTS));
        assertThat(homePageModel,hasKey(CURRENT_PAGE));
        assertThat(homePageModel,hasKey(ALL_POSTS_COUNT));
        
        assertHomeFirstPagePosts((List<Post>) homePageModel.get(HOME_POSTS));
        
        assertThat((Integer) homePageModel.get(ALL_POSTS_COUNT), is(NUMBER_OF_ALL_POSTS_IN_TEST));
        assertThat((Integer) homePageModel.get(CURRENT_PAGE), is(1));
        
        assertThat((String) homePageModel.get(TITLE), is("Home" + TITLE_BASE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void homePageByNumberGivesPostsForThatNumber() throws Exception {
        Map<String, Object> homePageModel = Blogix.homePage(2);
        
        assertCommonModelDataForPosts(homePageModel);
        
        assertThat(homePageModel,hasKey(HOME_POSTS));
        assertThat(homePageModel,hasKey(CURRENT_PAGE));
        assertThat(homePageModel,hasKey(ALL_POSTS_COUNT));
        
        List<Post> homePosts = (List<Post>) homePageModel.get(HOME_POSTS);
        assertHomeSecondPagePosts(homePosts);
        
        assertThat("'" + ALL_POSTS_COUNT + "' field in homePageModel should be", (Integer) homePageModel.get(ALL_POSTS_COUNT), is(NUMBER_OF_ALL_POSTS_IN_TEST));
        assertThat("'" + CURRENT_PAGE + "' field in homePageModel should be", (Integer) homePageModel.get(CURRENT_PAGE), is(1));
        
        assertThat((String) homePageModel.get(TITLE), is("Home" + TITLE_BASE));
    }
    
    
    @Test
    public void readsSingleBlogPostById() throws Exception {
        String postId = "2012-01-01-title-01";
        Map<String, Object> postModel = Blogix.post(postId);
        assertThat(postModel, hasKey("post"));
        assertCommonModelDataForPosts(postModel);
        
        Post post = (Post) postModel.get("post");
        
        assertThat("Post should be not null", post, is(notNullValue()));
        assertThat(post.getId(), is(postId));
        assertThat("title for post '" + postId + "' should be", post.getTitle(), is("Title 1"));
        assertThat("content for post '" + postId + "' should be", post.getContent(), is("Content 1\nPart 1\nContent 1 part 2"));
        assertThat("content preview part for '" + postId + "' should be", post.getContentPreview(), is("Content 1\nPart 1"));
        assertThat("commentsAllowed for post '" + postId + "' should be", post.getAllowComments(), is(true));
        assertThat("date for post '" + postId + "' should be", post.getDate(), is(new Date(1325374440000L)));
        assertThat("sections for post '" + postId + "' should be", post.getSections(), is(new String[]{"section1", "section2"}));
        assertThat("externalUrl for post '" + postId + "' should be", post.getExternalUrl(), is("www.google.com"));
        assertThat("externalUrl for post '" + postId + "' should be", post.getDisplayExternalUrl(), is("Google It!"));
        
        //Verifying that post can access additional custom fields
        assertThat("customField is incorrect for post '" + postId + "'", post.field("customField"), is("customValue"));
        
        assertThat((String) postModel.get(TITLE), is("Title 1" + TITLE_BASE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void searchesForPostsBySection() throws Exception {
        Map<String, Object> postsModel = Blogix.postsBySection("section1");
        assertThat(postsModel, hasKey("posts"));
        assertThat(postsModel, hasKey("section"));
        assertThat(postsModel, hasKey(CURRENT_PAGE));
        assertThat(postsModel, hasKey(ALL_POSTS_COUNT));
        
        assertCommonModelDataForPosts(postsModel);
        
        Section section = (Section) postsModel.get("section");
        assertThat(section.getId(), is("section1"));
        assertThat(section.getName(), is("Section 1"));
        
        List<Post> posts = (List<Post>) postsModel.get("posts");
        assertThat(posts.size(), is(NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST));
        
        for (int i = 1; i <= NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST; i++) {
            String strNumber = Integer.toString(i);
            if (i<10) {
                strNumber = "0" + strNumber;
            }
            assertThat(posts.get(i - 1).getId(), is("2012-01-01-title-" + strNumber));
        }
        
        assertThat((Integer)postsModel.get(ALL_POSTS_COUNT), is(12));
        assertThat((Integer)postsModel.get(CURRENT_PAGE), is(1));
        
        assertThat((String) postsModel.get(TITLE), is("Section 1" + TITLE_BASE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void searchesForPostsBySectionAndPage() throws Exception {
        Map<String, Object> postsModel = Blogix.postsBySectionAndPage("section1", 2);
        
        assertThat(postsModel, hasKey("posts"));
        assertThat(postsModel, hasKey("section"));
        assertThat(postsModel, hasKey(CURRENT_PAGE));
        assertThat(postsModel, hasKey(ALL_POSTS_COUNT));
        
        assertCommonModelDataForPosts(postsModel);
        
        Section section = (Section) postsModel.get("section");
        assertThat(section.getId(), is("section1"));
        assertThat(section.getName(), is("Section 1"));
        
        List<Post> posts = (List<Post>) postsModel.get("posts");
        assertThat(posts.size(), is(2));
        assertThat(posts.get(0).getId(), is("2012-01-01-title-11"));
        assertThat(posts.get(1).getId(), is("2012-01-01-title-12"));
        assertThat((Integer)postsModel.get(ALL_POSTS_COUNT), is(NUMBER_OF_ALL_POSTS_FOR_SECTION_1));
        assertThat((Integer)postsModel.get(CURRENT_PAGE), is(2));
        
        assertThat((String) postsModel.get(TITLE), is("Section 1" + TITLE_BASE));
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void rssFeedForAllPosts() throws Exception {
        Map<String, Object> rssModel = Blogix.rssFeedAll();
        assertThat(rssModel, hasKey("posts"));
        List<Post> posts = (List<Post>) rssModel.get("posts");
        assertThat("RSS feed for all should have all posts", posts.size(), is(NUMBER_OF_ALL_POSTS_IN_TEST));
        
        assertThat("First post should be", posts.get(0).getId(), is("2012-01-01-title-01"));
        assertThat("Second post should be", posts.get(1).getId(), is("2012-01-01-title-02"));
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void rssFeedForPostsBySection1() throws Exception {
        Map<String, Object> rssModel = Blogix.rssFeedForSection("section1");
        assertThat(rssModel, hasKey("posts"));
        List<Post> posts = (List<Post>) rssModel.get("posts");
        assertThat("RSS feed for all should have all posts", posts.size(), is(NUMBER_OF_ALL_POSTS_FOR_SECTION_1));
        
        assertThat("First post should be", posts.get(0).getId(), is("2012-01-01-title-01"));
        assertThat("Second post should be", posts.get(1).getId(), is("2012-01-01-title-02"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void rssFeedForPostsBySection3() throws Exception {
        Map<String, Object> rssModel = Blogix.rssFeedForSection("section3");
        assertThat(rssModel, hasKey("posts"));
        List<Post> posts = (List<Post>) rssModel.get("posts");
        assertThat("RSS feed for all should have all posts", posts.size(), is(NUMBER_OF_ALL_POSTS_FOR_SECTION_2));
        
        assertThat("First post should be", posts.get(0).getId(), is("2012-01-01-title-13"));
        assertThat("Second post should be", posts.get(1).getId(), is("2012-01-01-title-14"));
    }
    
    
    @Test
    public void readAttachmentForPost() throws Exception {
        File file = Blogix.fileForPost("2012-01-01-title-02", "file01.txt");
        assertThat("Attachment should not be null", file, is(notNullValue()));
        assertThat("Attachment should be named", file.getName(), is("2012-01-01-title-02.file01.txt"));
        assertThat("Attachment should exist", file.exists(), is(true));
    }
    
    private void assertRecentPosts(List<Post> recentPosts) {
        assertThat("List of recentPosts should be", recentPosts, is(notNullValue()));
        assertThat("Size of recentPosts should be", recentPosts.size(), is(DEFAULT_NUMBER_OF_RECENT_POSTS));
    }

    private void assertHomeFirstPagePosts(List<Post> homePosts) {
        assertThat("List of homePosts should be", homePosts, is(notNullValue()));
        assertThat("Size of homePosts should be", homePosts.size(), is(NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST));
        
        Iterator<Post> it = homePosts.iterator();
        int index = NUMBER_OF_ALL_POSTS_IN_TEST;
        while(it.hasNext()) {
            Post post = it.next();
            assertThat(post.getTitle(), is("Title " + (index)));
            index--;
        }
    }

    private void assertHomeSecondPagePosts(List<Post> homePosts) {
        assertThat("List of homePosts should be not null", homePosts, is(notNullValue()));
        assertThat("Size of homePosts should be", homePosts.size(), is(NUMBER_OF_SECOND_PAGE_POSTS_IN_TEST));
        
        Iterator<Post> it = homePosts.iterator();
        int index = NUMBER_OF_SECOND_PAGE_POSTS_IN_TEST;
        while(it.hasNext()) {
            Post post = it.next();
            assertThat(post.getTitle(), is("Title " + (index)));
            index--;
        }
    }
    
    
    
    @SuppressWarnings("unchecked")
    private void assertCommonModelDataForPosts(Map<String, Object> model) {
        assertThat(model, hasKey(RECENT_POSTS));
        assertThat(model, hasKey(TITLE));
        List<Post> recentPosts = (List<Post>) model.get(RECENT_POSTS);
        assertRecentPosts(recentPosts);
    }
        
}
