import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

const features = [
  {
    title: 'Scala library',
    imageUrl: 'img/scala_full_color.svg',
    description: (
      <>
        Forget about the <code>OSM Pbf</code> format complexity and think about a <strong>simple iterators of primitives (nodes, ways and relations) or blob blocks</strong>.
        <br/>
        Easy and high performance library, with millions of entities processed per second, in only one thread.
      </>
    ),
  },
  {
    title: 'Spark Polyglot',
    imageUrl: 'img/apache_spark_logo.svg',
    description: (
      <>
        Creating DataFrames representing OSM Pbf file contents
          from <strong>PySpark</strong>, <strong>Spark Scala</strong>, <strong>Spark SQL</strong> or <strong>SparkR</strong> is as simple as any other format file.
      </>
    ),
  },
  {
    title: 'Community / Commercial support',
    imageUrl: 'img/simplex_logo.png',
    description: (
      <>
        Two support ways:
        <ul>
          <li>Free support, via <a href="https://stackoverflow.com/questions/tagged/osm4scala" target="_blank">StackOverflow</a>.</li>
          <li>Commercial support, via the <a href="https://www.acervera.com" target="_blank">Author services</a>.</li>
        </ul>


      </>
    ),
  },
];

function Feature({imageUrl, title, description}) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx('col col--4', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

export default function Home() {
  const context = useDocusaurusContext();
  const {siteConfig = {}} = context;
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                'button button--outline button--secondary button--lg',
                styles.getStarted,
              )}
              to={useBaseUrl('docs/')}>
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {features && features.length > 0 && (
          <section className={styles.features}>
            <div className="container">
              <div className="row">
                {features.map((props, idx) => (
                  <Feature key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}
