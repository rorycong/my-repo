package splitter.client;

import splitter.client.controller.*;
import splitter.client.model.*;
import splitter.client.view.*;

import java.nio.file.Path;

/**
 * Main entry point for the Java client application. Wires models, controllers, and views.
 */
public class ClientMain {
    private final SessionModel session;
    private final ConnectionModel connection;
    private final GroupListModel groupListModel;
    private final GroupModel groupModel;
    private final OutstandingModel outstandingModel;
    private final BillFormModel billFormModel;

    private final ServerConnection serverConnection;
    private final AppController appController;
    private final UsernameController usernameController;
    private final HomeController homeController;
    private final SyncController syncController;
    private final GroupAdminController groupAdminController;
    private final BillActionController billActionController;

    private final ErrorMessageView errorMessageView;
    private final ConfirmDialogView confirmDialogView;
    private final ConnectionBannerView bannerView;
    private final CreateGroupView createGroupView;
    private final JoinGroupView joinGroupView;
    private final GroupCodePromptView promptView;
    private final UsernameView usernameView;
    private final HomeView homeView;
    private final MemberListView memberListView;
    private final BillListView billListView;
    private final BillDetailsView billDetailsView;
    private final CreateBillView createBillView;
    private final GroupView groupView;

    public ClientMain() {
        this(Path.of("session.cfg"));
    }

    public ClientMain(Path sessionConfigFile) {
        this.session = new SessionModel(sessionConfigFile);
        this.connection = new ConnectionModel();
        this.groupListModel = new GroupListModel();
        this.groupModel = new GroupModel();
        this.outstandingModel = new OutstandingModel(groupModel);
        this.billFormModel = new BillFormModel(groupModel, connection);

        this.errorMessageView = new ErrorMessageView();
        this.confirmDialogView = new ConfirmDialogView();
        this.bannerView = new ConnectionBannerView(connection);

        this.serverConnection = new ServerConnection(session, connection);
        this.appController = new AppController(session, groupModel, groupListModel, errorMessageView);

        this.usernameController = new UsernameController(serverConnection, session, appController, errorMessageView);
        this.homeController = new HomeController(serverConnection, session, groupModel, groupListModel, appController, errorMessageView);
        this.syncController = new SyncController(serverConnection, session, groupModel, groupListModel, appController);
        this.groupAdminController = new GroupAdminController(serverConnection, session, groupModel, appController, syncController, confirmDialogView, errorMessageView);
        this.billActionController = new BillActionController(serverConnection, groupModel, billFormModel, syncController, confirmDialogView, errorMessageView);

        this.createGroupView = new CreateGroupView(homeController);
        this.joinGroupView = new JoinGroupView(homeController, connection);
        this.promptView = new GroupCodePromptView(homeController);
        this.homeController.setGroupCodePromptView(promptView);

        this.usernameView = new UsernameView(usernameController, session, connection);
        this.homeView = new HomeView(homeController, groupListModel, session, connection, createGroupView, joinGroupView);

        this.memberListView = new MemberListView(groupModel, outstandingModel);
        this.billListView = new BillListView(billActionController, groupModel);
        this.billDetailsView = new BillDetailsView(billActionController, groupModel, connection);
        this.createBillView = new CreateBillView(billActionController, billFormModel);

        this.groupView = new GroupView(groupAdminController, groupModel, connection,
                memberListView, billListView, billDetailsView, createBillView, bannerView);

        this.appController.wire(usernameView, homeView, groupView, syncController);
    }

    public void start() {
        appController.start();
    }

    public AppController getAppController() {
        return appController;
    }

    public SessionModel getSession() {
        return session;
    }

    public ConnectionModel getConnection() {
        return connection;
    }

    public GroupModel getGroupModel() {
        return groupModel;
    }

    public HomeView getHomeView() {
        return homeView;
    }

    public GroupView getGroupView() {
        return groupView;
    }

    public UsernameView getUsernameView() {
        return usernameView;
    }

    public static void main(String[] args) {
        ClientMain client = new ClientMain();
        client.start();
        System.out.println("Shared Expense Splitter client started.");
    }
}
