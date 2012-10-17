package org.nescent.protege.cdao;

import java.awt.BorderLayout;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.list.OWLObjectList;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.individual.AbstractOWLIndividualViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

@SuppressWarnings("serial")
public class CharactersPanel extends AbstractOWLIndividualViewComponent {

	private OWLObjectList<OWLIndividual> list;
	private OWLOntologyChangeListener listener;

	private Set<OWLIndividual> individualsInList;
	private ChangeListenerMediator changeListenerMediator;

	private OWLModelManagerListener modelManagerListener;

	private ListSelectionListener listSelectionListener;

	private static final IRI CDAO_STANDARD_CHARACTER = IRI.create("http://purl.obolibrary.org/obo/CDAO_0000075");

	@Override
	public void initialiseIndividualsView() throws Exception {
		list = new OWLObjectList<OWLIndividual>(getOWLEditorKit());
		setLayout(new BorderLayout());
		add(new JScrollPane(list));

		listener = new OWLOntologyChangeListener() {
			public void ontologiesChanged(java.util.List<? extends OWLOntologyChange> changes) {
				//processChanges(changes);
			}
		};
		getOWLModelManager().addOntologyChangeListener(listener);
		individualsInList = new TreeSet<OWLIndividual>(getOWLModelManager().getOWLObjectComparator());
		changeListenerMediator = new ChangeListenerMediator();
		modelManagerListener = new OWLModelManagerListener() {

			public void handleChange(OWLModelManagerChangeEvent event) {
				if(event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED) || event.isType(EventType.ONTOLOGY_RELOADED)) {
					refill();
				}
			}
		};
		listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (list.getSelectedValue() != null) {
						setGlobalSelection((OWLNamedIndividual)list.getSelectedValue());
					}
					changeListenerMediator.fireStateChanged(CharactersPanel.this);
				}
			}
		};
		list.addListSelectionListener(listSelectionListener);
		getOWLModelManager().addListener(modelManagerListener);

		refill();

	}

	@Override
	public OWLNamedIndividual updateView(OWLNamedIndividual selelectedIndividual) {
		list.setSelectedValue(selelectedIndividual, true);
		return selelectedIndividual;
	}

	private void refill() {
		individualsInList.clear();
		for (OWLOntology ont : getOWLModelManager().getActiveOntologies()) {
			final OWLClass character = ont.getOWLOntologyManager().getOWLDataFactory().getOWLClass(CDAO_STANDARD_CHARACTER);
			final Set<OWLClassAssertionAxiom> axioms = ont.getClassAssertionAxioms(character);
			for (OWLClassAssertionAxiom axiom : axioms) {
				individualsInList.add(axiom.getIndividual());
			}
		}
		reset();
	}

	private void reset() {

		list.setListData(individualsInList.toArray());
		OWLEntity entity = getSelectedIndividual();
		if (entity instanceof OWLIndividual) {
			list.setSelectedValue(entity, true);
		}
	}

	public OWLNamedIndividual getSelectedIndividual() {
		return (OWLNamedIndividual) list.getSelectedValue();
	}

	//	private void processChanges(java.util.List<? extends OWLOntologyChange> changes) {
	//		Set<OWLEntity> possiblyAddedEntities = new HashSet<OWLEntity>();
	//		Set<OWLEntity> possiblyRemovedEntities = new HashSet<OWLEntity>();
	//		OWLEntityCollector addedCollector = new OWLEntityCollector(possiblyAddedEntities);
	//		OWLEntityCollector removedCollector = new OWLEntityCollector(possiblyRemovedEntities);
	//
	//		for(OWLOntologyChange chg : changes) {
	//			if(chg.isAxiomChange()) {
	//				OWLAxiomChange axChg = (OWLAxiomChange) chg;
	//				if(axChg instanceof AddAxiom) {
	//					axChg.getAxiom().accept(addedCollector);
	//				}
	//				else {
	//					axChg.getAxiom().accept(removedCollector);
	//				}
	//			}
	//		}
	//		boolean mod = false;
	//		for(OWLEntity ent : possiblyAddedEntities) {
	//			if(ent instanceof OWLIndividual) {
	//				if(individualsInList.add((OWLIndividual) ent)) {
	//					mod = true;
	//				}
	//			}
	//		}
	//		for(OWLEntity ent : possiblyRemovedEntities) {
	//			if(ent instanceof OWLNamedIndividual) {
	//				if(individualsInList.remove(ent)) {
	//					mod = true;
	//				}
	//			}
	//		}
	//		if(mod) {
	//			refill();
	//		}
	//	}

	@Override
	public void disposeView() {
		getOWLModelManager().removeOntologyChangeListener(listener);
		getOWLModelManager().removeListener(modelManagerListener);
	}





}
