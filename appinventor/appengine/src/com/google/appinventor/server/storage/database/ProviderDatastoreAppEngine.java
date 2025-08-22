package com.google.appinventor.server.storage.database;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.storage.ObjectifyException;
import com.google.appinventor.server.storage.StoredData;
import com.google.common.annotations.VisibleForTesting;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class ProviderDatastoreAppEngine extends DatabaseService {

  private static final Logger LOG = Logger.getLogger(ProviderDatastoreAppEngine.class.getName());

  // TODO(user): need a way to modify this. Also, what is really a good value?
  private static final int MAX_JOB_RETRIES = 10;

  public ProviderDatastoreAppEngine() {
    ObjectifyService.register(StoredData.UserData.class);
    ObjectifyService.register(StoredData.ProjectData.class);
    ObjectifyService.register(StoredData.UserProjectData.class);
    ObjectifyService.register(StoredData.FileData.class);
    ObjectifyService.register(StoredData.UserFileData.class);
    ObjectifyService.register(StoredData.RendezvousData.class);
    ObjectifyService.register(StoredData.WhiteListData.class);
    ObjectifyService.register(StoredData.FeedbackData.class);
    ObjectifyService.register(StoredData.NonceData.class);
    ObjectifyService.register(StoredData.CorruptionRecord.class);
    ObjectifyService.register(StoredData.PWData.class);
    ObjectifyService.register(StoredData.SplashData.class);
    ObjectifyService.register(StoredData.Backpack.class);
    ObjectifyService.register(StoredData.AllowedTutorialUrls.class);
    ObjectifyService.register(StoredData.AllowedIosExtensions.class);
  }

  @VisibleForTesting
  abstract class JobRetryHelper {
    private IOException exception = null;
    public abstract void run(Objectify datastore) throws ObjectifyException, IOException;
    /*
     * Called before retrying the job. Note that the underlying datastore
     * still has the transaction active, so restrictions about operations
     * over multiple entity groups still apply.
     */
    public void onNonFatalError() {
      // Default is to do nothing
    }
    public void onIOException(IOException error) {
      exception = error;
    }
    public IOException getIOException() {
      return exception;
    }
  }

  @Override
  public StoredData.UserData findOrCreateUser(final String userId, final String email) {
    final AtomicReference<StoredData.UserData> finalUserData = new AtomicReference<>();

    try {
      runJobWithRetries(new JobRetryHelper() {
        @Override
        public void run(Objectify datastore) {
          StoredData.UserData userData = datastore.find(userKey(userId));
          boolean viaemail = false; // Which datastore copy did we find it with...
          Objectify qDatastore = null;
          if (userData == null) { // Attempt to find them by email
            LOG.info("Did not find userId " + userId);
            if (email != null) {
              qDatastore = ObjectifyService.begin(); // Need an instance not in this transaction
              userData = qDatastore.query(StoredData.UserData.class).filter("email", email).get();
              if (userData == null) { // Still null!
                userData = qDatastore.query(StoredData.UserData.class).filter("emaillower", email.toLowerCase()).get();
              }

              // Need to fix userId...
              if (userData != null) {
                LOG.info("Found based on email, userData.id = " + userData.id);
                viaemail = true;
              }
            }
            if (userData == null) { // No joy, create it.
              userData = createUser(datastore, userId, email);
            }
          } else if (email != null && !email.equals(userData.email)) {
            userData.email = email;
            userData.emaillower = email.toLowerCase();
            datastore.put(userData);
          }

          // Add emaillower if it isn't already there
          if (userData.emaillower == null) {
            userData.emaillower = userData.email.toLowerCase();
            if (viaemail) {
              qDatastore.put(userData);
            } else {
              datastore.put(userData);
            }
          }

          finalUserData.set(userData);
        }
      }, false);                // Transaction not needed. If we fail there is nothing to rollback
    } catch (ObjectifyException e) {
      throw CrashReport.createAndLogError(LOG, null, collectUserErrorInfo(userId), e);
    }

    return finalUserData.get();
  }

  private StoredData.UserData createUser(Objectify datastore, String userId, String email) {
    String emaillower = null;
    if (email != null) {
      emaillower = email.toLowerCase();
    }
    StoredData.UserData userData = new StoredData.UserData();
    userData.id = userId;
    userData.tosAccepted = false;
    userData.settings = "";
    userData.email = email == null ? "" : email;
    userData.emaillower = email == null ? "" : emaillower;
    datastore.put(userData);
    return userData;
  }

  @VisibleForTesting
  void runJobWithRetries(JobRetryHelper job, boolean useTransaction) throws ObjectifyException {
    int tries = 0;
    while (tries <= MAX_JOB_RETRIES) {
      Objectify datastore;
      if (useTransaction) {
        datastore = ObjectifyService.beginTransaction();
      } else {
        datastore = ObjectifyService.begin();
      }
      try {
        job.run(datastore);
        if (useTransaction) {
          datastore.getTxn().commit();
        }
        break;
      } catch (ConcurrentModificationException ex) {
        job.onNonFatalError();
        LOG.log(Level.WARNING, "Optimistic concurrency failure", ex);
      } catch (ObjectifyException oe) {
        String message = oe.getMessage();
        if (message != null &&
            (message.startsWith("Blocks") || message.startsWith("User Al"))) { // This one is fatal!
          throw oe;
        }
        // maybe this should be a fatal error? I think only thing
        // that creates this exception is this method.
        job.onNonFatalError();
      } catch (IOException e) {
        job.onIOException(e);
        break;
      } finally {
        if (useTransaction && datastore.getTxn().isActive()) {
          try {
            datastore.getTxn().rollback();
          } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Transaction rollback failed", e);
          }
        }
      }
      tries++;
    }
    if (tries > MAX_JOB_RETRIES) {
      throw new ObjectifyException("Couldn't commit job after max retries.");
    }
  }

  private Key<StoredData.UserData> userKey(String userId) {
    return new Key<>(StoredData.UserData.class, userId);
  }
}
