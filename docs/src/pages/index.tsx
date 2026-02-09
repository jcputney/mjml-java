import clsx from 'clsx';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import CodeBlock from '@theme/CodeBlock';

import styles from './index.module.css';

const features = [
  {
    title: 'Zero Dependencies',
    description:
      'Pure Java implementation with no external runtime dependencies. Just add the JAR and go.',
  },
  {
    title: 'All 31 Components',
    description:
      'Full support for every top-level MJML renderable component, from mj-section and mj-column to mj-accordion and mj-navbar.',
  },
  {
    title: 'Thread-Safe',
    description:
      'Designed for concurrent use in server applications. Render multiple templates simultaneously without shared mutable state.',
  },
  {
    title: 'JPMS Module',
    description:
      'First-class Java Platform Module System support. Works seamlessly with modular Java applications.',
  },
  {
    title: 'CSS Inliner',
    description:
      'Built-in CSS inlining engine that handles mj-style, mj-attributes, and inline styles with proper specificity.',
  },
  {
    title: 'Custom Components',
    description:
      'Extend the renderer with your own MJML components using a simple registration API.',
  },
];

const quickStartCode = `// Add to your pom.xml
// <dependency>
//   <groupId>dev.jcputney</groupId>
//   <artifactId>mjml-java-core</artifactId>
//   <version>1.0.0-SNAPSHOT</version>
// </dependency>

import dev.jcputney.mjml.MjmlRenderer;

String mjml = """
  <mjml>
    <mj-body>
      <mj-section>
        <mj-column>
          <mj-text>Hello World!</mj-text>
        </mj-column>
      </mj-section>
    </mj-body>
  </mjml>
  """;

String html = MjmlRenderer.render(mjml);`;

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <a
            className="button button--secondary button--lg"
            href="/mjml-java/docs/getting-started/installation">
            Get Started
          </a>
        </div>
      </div>
    </header>
  );
}

function Feature({title, description}: {title: string; description: string}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center padding-horiz--md padding-vert--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {features.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}

function QuickStart() {
  return (
    <section className={styles.quickStart}>
      <div className="container">
        <div className="row">
          <div className="col col--8 col--offset-2">
            <Heading as="h2" className="text--center margin-bottom--lg">
              Quick Start
            </Heading>
            <CodeBlock language="java">{quickStartCode}</CodeBlock>
          </div>
        </div>
      </div>
    </section>
  );
}

export default function Home(): JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={siteConfig.title}
      description="Pure Java MJML-to-HTML renderer - zero dependencies, all 31 components, thread-safe">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
        <QuickStart />
      </main>
    </Layout>
  );
}
