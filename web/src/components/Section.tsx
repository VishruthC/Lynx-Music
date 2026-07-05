import { useNavigate } from 'react-router-dom';
import type { GenericDto } from '../types';
import './Section.css';

interface SectionProps {
  title: string;
  items: GenericDto[];
}

const Section = ({ title, items }: SectionProps) => {
  const navigate = useNavigate();

  const handleItemClick = (item: GenericDto) => {
    if (item.type === 'playlist') {
      navigate(`/playlist/${item.id}`);
    } else if (item.type === 'album') {
      navigate(`/album/${item.id}`);
    }
  };

  return (
    <section className="content-section">
      <h3>{title}</h3>
      <div className="items-grid">
        {items.map((item) => (
          <div key={item.id} className="grid-item" onClick={() => handleItemClick(item)}>
            <div className="img-wrapper">
              <img src={item.image[item.image.length - 1]?.url} alt={item.title || item.name} />
              <button className="play-overlay">
                <svg viewBox="0 0 24 24" width="24" height="24">
                  <path d="M7 6v12l10-6z" fill="black" />
                </svg>
              </button>
            </div>
            <div className="item-info">
              <div className="item-title">{item.title || item.name}</div>
              <div className="item-desc">{item.description}</div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

export default Section;
