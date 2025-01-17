package hudson.plugins.favorite;

import hudson.Plugin;
import hudson.model.Item;
import hudson.model.User;
import hudson.plugins.favorite.Favorites.FavoriteException;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;

@Restricted(NoExternalUse.class)
public class FavoritePlugin extends Plugin {

    @RequirePOST
    public void doToggleFavorite(StaplerRequest req, StaplerResponse resp, @QueryParameter String job, @QueryParameter Boolean redirect) throws IOException {
        Jenkins jenkins = Jenkins.get();
        User user = User.current();
        if (user != null) {
            try {
                Favorites.toggleFavorite(user, getItem(job));
            } catch (FavoriteException e) {
                throw new IOException(e);
            }
        }
        if(redirect) {
            if (!job.contains("/"))
            {
              // Works for default URL pattern: rootUrl/job/jobName
              resp.sendRedirect(resp.encodeRedirectURL(jenkins.getRootUrl() + "job/" + job));
            }
            else
            {
              // Works for folder URL pattern:
              // rootUrl/job/folder/job/jobName
              // rootUrl/job/folder/job/subfolder/job/jobName etc.
              StringBuilder urlPostfix = new StringBuilder("job/");
              String[] itemNames = job.split("/");
              for (int i = 0; i < itemNames.length; i++)
              {
                urlPostfix.append(itemNames[i]);                  
                if (i < itemNames.length - 1)
                {
                  urlPostfix.append("/job/");
                }
              }
              resp.sendRedirect(resp.encodeRedirectURL(jenkins.getRootUrl() + urlPostfix.toString()));
            }
        }
    }

    public static Item getItem(String fullName) {
        Jenkins jenkins = Jenkins.get();
        Item item = jenkins.getItemByFullName(fullName);
        if (item == null) {
            throw new IllegalArgumentException("Item <" + fullName + "> does not exist");
        }
        return item;
    }
}
