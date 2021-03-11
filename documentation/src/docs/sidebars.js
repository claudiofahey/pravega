module.exports = {
    mainSidebar: [
        'index',
        'getting-started',
        {
            'Understanding Pravega': [
                'pravega-concepts',
                'terminology',
                'key-features',
                'faq',
                'segment-store-service',
                'segment-containers',
                'controller-service',
                'wire-protocol',
                'state-synchronizer-design',
                'reader-group-design',
                'watermarking',
            ],
            'Developing Pravega Applications': [
                'javadoc',
                'rest/restapis',
                'connectors',
                'basic-reader-and-writer',
                'state-synchronizer',
                'transactions',
                'streamcuts',
            ],
            'Running Pravega': [
                'deployment/deployment',
                'deployment/manual-install',
                'deployment/kubernetes-install',
                'deployment/docker-swarm',
                'deployment/dcos-install',
                'deployment/aws-install',
                'metrics',
            ],
            'Pravega Security': [
                'auth/auth-plugin',
                'auth/client-auth',
                'security/pravega-security-authorization-authentication',
                'security/pravega-security-configurations',
                'security/pravega-security-encryption',
                'security/securing-distributed-mode-cluster',
            ],
            'Contributing to Pravega': [
                'contributing',
                'roadmap',
                'join-community',
            ]
        },
    ]
};
