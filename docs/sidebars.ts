import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  docsSidebar: [
    {
      type: 'category',
      label: 'Getting Started',
      collapsed: false,
      items: [
        'getting-started/installation',
        'getting-started/quick-start',
        'getting-started/configuration',
      ],
    },
    {
      type: 'category',
      label: 'API Reference',
      items: [
        'api-reference/index',
        'api-reference/mjml-renderer',
        'api-reference/mjml-configuration',
        'api-reference/include-resolver',
        'api-reference/css-inliner',
      ],
    },
    {
      type: 'category',
      label: 'Guides',
      items: [
        'guides/custom-components',
        'guides/include-system',
        'guides/attribute-cascade',
        'guides/resolvers',
        'guides/spring-boot',
        'guides/security',
        'guides/thread-safety',
      ],
    },
    {
      type: 'category',
      label: 'Architecture',
      items: [
        'architecture/index',
        'architecture/pipeline',
        'architecture/css-engine',
      ],
    },
    'supported-components',
    'contributing',
    'faq',
  ],
};

export default sidebars;
