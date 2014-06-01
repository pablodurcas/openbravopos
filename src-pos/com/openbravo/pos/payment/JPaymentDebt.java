//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.payment;

import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.util.RoundUtils;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author  adrianromero
 */
public class JPaymentDebt extends javax.swing.JPanel implements JPaymentInterface {
    
    private JPaymentNotifier notifier;
    private CustomerInfoExt customerext;
    
    private double m_dPaid;
    private double m_dPaid_Depo;
    private double m_dPaid_Debt;
    private double m_dTotal;

    /** Creates new form JPaymentDebt */
    public JPaymentDebt(JPaymentNotifier notifier) {
        
        this.notifier = notifier;
        
        initComponents();  
        
        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);
        
    }
    
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {
        
        this.customerext = customerext;
        m_dTotal = dTotal;
        
        m_jTendered.reset();
        
        // 
        if (customerext == null) {
            m_jName.setText(null);
            m_jNotes.setText(null);
            txtMaxdebt.setText(null);
            txtCurdate.setText(null);        
            txtCurdebt.setText(null);
            txtCurdepo.setText(null);
            txtCurdepodate.setText(null);
            m_jKeys.setEnabled(false);
            m_jTendered.setEnabled(false);
            
            
        } else {            
            m_jName.setText(customerext.getName());
            m_jNotes.setText(customerext.getNotes());
            txtMaxdebt.setText(Formats.CURRENCY.formatValue(RoundUtils.getValue(customerext.getMaxdebt())));
            txtCurdate.setText(Formats.DATE.formatValue(customerext.getCurdate()));        
            txtCurdebt.setText(Formats.CURRENCY.formatValue(RoundUtils.getValue(customerext.getCurdebt())));   
            txtCurdepodate.setText(Formats.DATE.formatValue(customerext.getCurdepodate()));        
            txtCurdepo.setText(Formats.CURRENCY.formatValue(RoundUtils.getValue(customerext.getCurdepo())));   

            if (RoundUtils.compare(RoundUtils.getValue(customerext.getCurdebt()), RoundUtils.getValue(customerext.getMaxdebt())+RoundUtils.getValue(customerext.getCurdepo()) ) >= 0)  {
                m_jKeys.setEnabled(false);
                m_jTendered.setEnabled(false);                
            } else {    
                m_jKeys.setEnabled(true);
                m_jTendered.setEnabled(true);
                m_jTendered.activate();  
            }
        }        
        
        printState();
        
    }
    public PaymentInfo executePayment() {
        if(RoundUtils.compare(RoundUtils.getValue(m_dPaid_Debt),RoundUtils.getValue(0.0)) > 0)
            return new PaymentInfoTicket(m_dPaid_Debt, "debt");      
        else
            return null;
    }
    
    public PaymentInfo executePaymentDepo() {
        if(RoundUtils.compare(RoundUtils.getValue(m_dPaid_Depo),RoundUtils.getValue(0.0)) > 0)
            return new PaymentInfoTicket(m_dPaid_Depo, "debtDepo");
        else
            return null;
    }
    
    public Component getComponent() {
        return this;
    }

    private void printState() {
        
        if (customerext == null) {
            m_jMoneyEuros.setText(null);
            jlblMessage.setText(AppLocal.getIntString("message.nocustomernodebt"));
            notifier.setStatus(false, false);
        } else {
            Double value = m_jTendered.getDoubleValue();
            if (value == null || value == 0.0) {
                m_dPaid = m_dTotal;
               
            } else {
                m_dPaid = value;
            } 
            
            double curdepo = customerext.getCurdepo() == null ? 0.0 : customerext.getCurdepo();
            double curdebt = customerext.getCurdebt() == null ? 0.0 : customerext.getCurdebt();
            double maxdebt = customerext.getMaxdebt() == null ? 0.0 : customerext.getMaxdebt();
            
            if (RoundUtils.compare(RoundUtils.getValue(m_dPaid - curdepo),RoundUtils.getValue(0.0)) >= 0){
                m_dPaid_Debt = RoundUtils.getValue(m_dPaid - curdepo);
                m_dPaid_Depo = curdepo;
            }else{
                m_dPaid_Debt = 0.0;
                //m_dPaid_Depo = -RoundUtils.getValue((m_dPaid - customerext.getCurdepo()));
                m_dPaid_Depo = RoundUtils.getValue(m_dPaid);
                
            }
            m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
            
            
            if (RoundUtils.compare(RoundUtils.getValue(curdebt) + m_dPaid - RoundUtils.getValue(curdepo), RoundUtils.getValue(maxdebt)) >= 0)  { 
                // maximum debt exceded
                jlblMessage.setText(AppLocal.getIntString("message.customerdebtexceded"));
                notifier.setStatus(false, false);
            } else {
                jlblMessage.setText(null);
                int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);
                // if iCompare > 0 then the payment is not valid
                notifier.setStatus(m_dPaid > 0.0 && iCompare <= 0, iCompare == 0);
            }
        }        
    }
    
    private class RecalculateState implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }     
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        m_jMoneyEuros = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtMaxdebt = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtCurdebt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtCurdepodate = new javax.swing.JTextField();
        txtCurdate = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jNotes = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        txtCurdepo = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jlblMessage = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();

        setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(null);

        jLabel8.setText(AppLocal.getIntString("label.debt")); // NOI18N
        jPanel4.add(jLabel8);
        jLabel8.setBounds(20, 20, 100, 17);

        m_jMoneyEuros.setBackground(new java.awt.Color(153, 153, 255));
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        jPanel4.add(m_jMoneyEuros);
        m_jMoneyEuros.setBounds(120, 20, 150, 25);

        jLabel3.setText(AppLocal.getIntString("label.name")); // NOI18N
        jPanel4.add(jLabel3);
        jLabel3.setBounds(20, 50, 100, 17);

        m_jName.setEditable(false);
        jPanel4.add(m_jName);
        m_jName.setBounds(120, 50, 200, 27);

        jLabel12.setText(AppLocal.getIntString("label.notes")); // NOI18N
        jPanel4.add(jLabel12);
        jLabel12.setBounds(20, 80, 100, 17);

        jLabel2.setText(AppLocal.getIntString("label.maxdebt")); // NOI18N
        jPanel4.add(jLabel2);
        jLabel2.setBounds(20, 120, 100, 17);

        txtMaxdebt.setEditable(false);
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel4.add(txtMaxdebt);
        txtMaxdebt.setBounds(120, 120, 130, 27);

        jLabel4.setText(AppLocal.getIntString("label.curdebt")); // NOI18N
        jPanel4.add(jLabel4);
        jLabel4.setBounds(20, 150, 100, 17);

        txtCurdebt.setEditable(false);
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel4.add(txtCurdebt);
        txtCurdebt.setBounds(120, 150, 130, 27);

        jLabel6.setText(AppLocal.getIntString("label.curdate")); // NOI18N
        jPanel4.add(jLabel6);
        jLabel6.setBounds(20, 180, 100, 17);

        txtCurdepodate.setEditable(false);
        txtCurdepodate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel4.add(txtCurdepodate);
        txtCurdepodate.setBounds(120, 240, 130, 27);

        txtCurdate.setEditable(false);
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel4.add(txtCurdate);
        txtCurdate.setBounds(120, 180, 130, 27);

        jLabel5.setText(AppLocal.getIntString("label.curdepo")); // NOI18N
        jPanel4.add(jLabel5);
        jLabel5.setBounds(20, 210, 100, 17);

        m_jNotes.setEditable(false);
        jScrollPane1.setViewportView(m_jNotes);

        jPanel4.add(jScrollPane1);
        jScrollPane1.setBounds(120, 80, 200, 40);

        jLabel7.setText(AppLocal.getIntString("label.curdepodate")); // NOI18N
        jPanel4.add(jLabel7);
        jLabel7.setBounds(20, 240, 100, 17);

        txtCurdepo.setEditable(false);
        txtCurdepo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel4.add(txtCurdepo);
        txtCurdepo.setBounds(120, 210, 130, 27);

        jPanel5.add(jPanel4, java.awt.BorderLayout.CENTER);

        jlblMessage.setEditable(false);
        jlblMessage.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        jlblMessage.setLineWrap(true);
        jlblMessage.setWrapStyleWord(true);
        jlblMessage.setFocusable(false);
        jlblMessage.setPreferredSize(new java.awt.Dimension(300, 72));
        jlblMessage.setRequestFocusEnabled(false);
        jPanel6.add(jlblMessage);

        jPanel5.add(jPanel6, java.awt.BorderLayout.SOUTH);

        add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jlblMessage;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextArea m_jNotes;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtCurdepo;
    private javax.swing.JTextField txtCurdepodate;
    private javax.swing.JTextField txtMaxdebt;
    // End of variables declaration//GEN-END:variables
    
}
