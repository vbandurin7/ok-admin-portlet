package unit.ok.admin.portlet;

public class PortletTestUtil {

    public static final String INSERTER_CONFIG = """
            WEB={
                chunkSize=26
                portletsByPosition={
                    {
                        position=0
                        emptyProbability=0
                        candidates={
                            {
                                category=CUSTOM_PORTLET
                                enabled=0-255
                                candidates=HobbyTutorials
                                weight=true
                                showProbability=100
                            }
                        }
                    }
                }
            }
            ANDROID={
                chunkSize=26
                portletsByPosition={
                    {
                        position=0
                        emptyProbability=0
                        candidates={
                            {
                                category=CUSTOM_PORTLET
                                enabled=0-255
                                candidates=HobbyTutorials
                                weight=true
                                showProbability=100
                            }
                        }
                    }
                }
            }
            IOS={
                chunkSize=26
                portletsByPosition={
                    {
                        position=0
                        emptyProbability=0
                        candidates={
                            {
                                category=CUSTOM_PORTLET
                                enabled=0-255
                                candidates=HobbyTutorials
                                weight=true
                                showProbability=100
                            }
                        }
                    }
                }
            }""";

    public static final String INSERTER_CONFIG_ANDROID = """
            ANDROID={
                chunkSize=26
                portletsByPosition={
                    {
                        position=0
                        emptyProbability=0
                        candidates={
                            {
                                category=CUSTOM_PORTLET
                                enabled=0-255
                                candidates=HobbyTutorials
                                weight=true
                                showProbability=100
                            }
                        }
                    }
                }
            }""";

    public static final String RESOLVER_CONFIG = """
            key.adminTest=601457556744
            key.banner_test=573817203042,573817202785,594434225942
            key.portletsAutotests=ID:588056360039,ID:601391123224
            key.kirill_orlov=580504923246,598687934483,584788966243,587207515860,529413883693,584980015437,581027176284,590978017675
            key.autotestBots=578186164430,578186410190,578193423566,578202171598,575648543445,575661603541,575661692373,575669414869,575669859797,575682949845,575717524181,575717989333,575721612501,575731910101,575736139733,575739217365,589197106230,573635747174,574047520846,593994401302,582554714799,573869207156,577861520540,577861520786,577532777426,581287426493,581410749366,582397219769
            key.discoveryBlinov=578734392665
            key.VideoPromoOnZero=576793440354,592791734322,583652852364
            key.news-on-top=589859366032,592319306694,605432507683,589528036319,584853747305""";
}
